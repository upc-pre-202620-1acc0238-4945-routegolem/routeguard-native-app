package pe.edu.upc.routeguard.core.network

import pe.edu.upc.routeguard.core.database.AbsenceReportEntity
import pe.edu.upc.routeguard.core.database.AuthorizedCaregiverEntity
import pe.edu.upc.routeguard.core.database.ChatMessageEntity
import pe.edu.upc.routeguard.core.database.OperationalExpenseEntity
import pe.edu.upc.routeguard.core.database.PickupChangeEntity
import pe.edu.upc.routeguard.core.database.PlatformCacheDao
import pe.edu.upc.routeguard.core.database.RouteAnnouncementEntity
import pe.edu.upc.routeguard.core.database.SupportTicketEntity
import pe.edu.upc.routeguard.core.database.TripRatingEntity
import pe.edu.upc.routeguard.core.database.UserAvatarCacheEntity
import pe.edu.upc.routeguard.core.database.VehicleDocsCacheEntity
import pe.edu.upc.routeguard.core.database.VehicleMaintenanceEntity
import pe.edu.upc.routeguard.core.session.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlatformFeaturesRepository @Inject constructor(
    private val api: PlatformFeaturesApi,
    private val dao: PlatformCacheDao,
    private val sessionManager: SessionManager
) {
    private fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private fun nowIso(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

    // ---- Profile Photo & Language ----

    suspend fun loadMyPhoto(): String? {
        val uid = sessionManager.current()?.userId ?: return null
        val cached = dao.getAvatar(uid)
        safeApiCall { api.getMe() }.onSuccess { user ->
            val resolvedPhoto = if (cached?.pendingSync == true) cached.photoData else user.photoData
            dao.upsertAvatar(
                UserAvatarCacheEntity(
                    userId = uid,
                    photoData = resolvedPhoto,
                    preferredLanguage = sessionManager.language(),
                    pendingSync = cached?.pendingSync == true
                )
            )
            return resolvedPhoto
        }
        return cached?.photoData
    }

    suspend fun saveMyPhoto(photoData: String?): Boolean {
        val uid = sessionManager.current()?.userId ?: "offline_user"
        dao.upsertAvatar(
            UserAvatarCacheEntity(
                userId = uid,
                photoData = photoData,
                preferredLanguage = sessionManager.language(),
                pendingSync = true
            )
        )
        return safeApiCall { api.updateMyPhoto(UpdatePhotoRequestDto(photoData)) }.fold(
            onSuccess = {
                dao.upsertAvatar(
                    UserAvatarCacheEntity(
                        userId = uid,
                        photoData = it.photoData ?: photoData,
                        preferredLanguage = sessionManager.language(),
                        pendingSync = false
                    )
                )
                true
            },
            onFailure = { false }
        )
    }

    suspend fun syncLanguage(language: String) {
        safeApiCall { api.updateMyLanguage(UpdateLanguageRequestDto(language)) }
    }

    // ---- US-19: Password Recovery ----

    suspend fun requestPasswordReset(email: String): Result<ForgotPasswordResponseDto> =
        safeApiCall { api.forgotPassword(ForgotPasswordRequestDto(email.trim())) }

    suspend fun confirmPasswordReset(email: String, code: String, newPassword: String): Result<Unit> =
        safeApiCall { api.resetPassword(ResetPasswordRequestDto(email.trim(), code.trim(), newPassword)) }

    // ---- US-12: Vehicle SOAT Docs ----

    suspend fun loadPrimaryVehicleDocs(): VehicleDocsCacheEntity? {
        safeApiCall { api.getVehicles() }.onSuccess { list ->
            val entities = list.map {
                VehicleDocsCacheEntity(
                    id = it.id,
                    plate = it.plate,
                    brand = it.brand ?: "Mercedes-Benz",
                    model = it.model,
                    capacity = it.capacity,
                    soatPolicy = it.soatPolicy ?: "SOAT-2026-88412",
                    soatInsurer = it.soatInsurer ?: "Rímac Seguros",
                    soatExpiresAt = it.soatExpiresAt ?: "2026-12-15",
                    soatValid = it.soatValid ?: true
                )
            }
            if (entities.isNotEmpty()) dao.upsertVehicles(entities)
        }
        return dao.getVehicles().firstOrNull()
    }

    // ---- Caregivers ----

    suspend fun getCaregivers(): List<AuthorizedCaregiverEntity> {
        syncPendingCaregivers()
        safeApiCall { api.getCaregivers() }.onSuccess { list ->
            dao.clearSyncedCaregivers()
            dao.upsertCaregivers(list.map {
                AuthorizedCaregiverEntity(
                    id = it.id,
                    organizationId = it.organizationId,
                    parentId = it.parentId,
                    fullName = it.fullName,
                    relationship = it.relationship,
                    phoneNumber = it.phoneNumber,
                    documentNumber = it.documentNumber,
                    photoData = it.photoData,
                    createdAt = it.createdAt,
                    pendingSync = false
                )
            })
        }
        return dao.getCaregivers()
    }

    suspend fun addCaregiver(fullName: String, relationship: String, phone: String): AuthorizedCaregiverEntity {
        val localId = "local_${UUID.randomUUID()}"
        val session = sessionManager.current()
        val localEntity = AuthorizedCaregiverEntity(
            id = localId,
            organizationId = session?.organizationId.orEmpty(),
            parentId = session?.profileId.orEmpty(),
            fullName = fullName.trim(),
            relationship = relationship.trim(),
            phoneNumber = phone.trim(),
            documentNumber = null,
            photoData = null,
            createdAt = nowIso(),
            pendingSync = true
        )
        dao.upsertCaregiver(localEntity)
        safeApiCall { api.addCaregiver(CreateCaregiverRequestDto(localEntity.fullName, localEntity.relationship, localEntity.phoneNumber)) }
            .onSuccess { remote ->
                dao.deleteCaregiver(localId)
                dao.upsertCaregiver(
                    AuthorizedCaregiverEntity(
                        id = remote.id,
                        organizationId = remote.organizationId,
                        parentId = remote.parentId,
                        fullName = remote.fullName,
                        relationship = remote.relationship,
                        phoneNumber = remote.phoneNumber,
                        documentNumber = remote.documentNumber,
                        photoData = remote.photoData,
                        createdAt = remote.createdAt,
                        pendingSync = false
                    )
                )
                return dao.getCaregivers().firstOrNull { it.id == remote.id } ?: localEntity
            }
        return localEntity
    }

    suspend fun deleteCaregiver(id: String) {
        dao.deleteCaregiver(id)
        if (!id.startsWith("local_")) {
            safeApiCall { api.deleteCaregiver(id) }
        }
    }

    // ---- US-23: Support Tickets ----

    suspend fun getSupportTickets(): List<SupportTicketEntity> {
        syncPendingTickets()
        safeApiCall { api.getSupportTickets() }.onSuccess { list ->
            dao.upsertSupportTickets(list.map {
                SupportTicketEntity(it.id, it.ticketCode, it.category, it.subject, it.message, it.status, it.createdAt, false)
            })
        }
        return dao.getSupportTickets()
    }

    suspend fun createSupportTicket(category: String, subject: String, message: String): SupportTicketEntity {
        val localId = "local_${UUID.randomUUID()}"
        val code = "#RG-${1000 + (System.currentTimeMillis() % 8999).toInt()}"
        val local = SupportTicketEntity(localId, code, category, subject.trim(), message.trim(), "OPEN", nowIso(), true)
        dao.upsertSupportTicket(local)
        safeApiCall { api.createSupportTicket(CreateSupportTicketRequestDto(category, subject.trim(), message.trim())) }
            .onSuccess { remote ->
                dao.deleteSupportTicket(localId)
                val synced = SupportTicketEntity(remote.id, remote.ticketCode, remote.category, remote.subject, remote.message, remote.status, remote.createdAt, false)
                dao.upsertSupportTicket(synced)
                return synced
            }
        return local
    }

    // ---- US-14: Trip Ratings ----

    suspend fun getTripRating(tripId: String): TripRatingEntity? = dao.getTripRating(tripId)

    suspend fun rateTrip(tripId: String, stars: Int, comment: String): TripRatingEntity {
        val clamped = stars.coerceIn(1, 5)
        val local = TripRatingEntity(tripId, clamped, comment.trim(), nowIso(), true)
        dao.upsertTripRating(local)
        safeApiCall { api.rateTrip(tripId, RateTripRequestDto(clamped, comment.trim())) }.onSuccess {
            val synced = local.copy(pendingSync = false)
            dao.upsertTripRating(synced)
            return synced
        }
        return local
    }

    // ---- US-09 & US-28: Absences + Route Stop Reassignment ----

    suspend fun getAbsences(): List<AbsenceReportEntity> {
        syncPendingAbsences()
        safeApiCall { api.getAbsences() }.onSuccess { list ->
            dao.upsertAbsences(list.map {
                AbsenceReportEntity(it.id, it.childId, it.childName, it.routeId, it.absenceDate, it.reason, it.notes, it.createdAt, false)
            })
        }
        return dao.getAbsences()
    }

    suspend fun reportAbsence(childId: String?, childName: String, routeId: String?, reason: String, notes: String?): AbsenceReportEntity {
        val localId = "local_${UUID.randomUUID()}"
        val date = todayIso()
        val local = AbsenceReportEntity(localId, childId.orEmpty(), childName.trim(), routeId.orEmpty(), date, reason.trim(), notes?.trim(), nowIso(), true)
        dao.upsertAbsence(local)
        safeApiCall { api.createAbsence(CreateAbsenceRequestDto(childId, childName.trim(), routeId, date, reason.trim(), notes?.trim())) }
            .onSuccess { remote ->
                dao.deleteAbsence(localId)
                val synced = AbsenceReportEntity(remote.id, remote.childId, remote.childName, remote.routeId, remote.absenceDate, remote.reason, remote.notes, remote.createdAt, false)
                dao.upsertAbsence(synced)
                return synced
            }
        return local
    }

    suspend fun getOptimizedStops(routeId: String): OptimizedRouteDto? =
        safeApiCall { api.getOptimizedStops(routeId, todayIso()) }.getOrNull()

    // ---- US-20: Vehicle Maintenances ----

    suspend fun getMaintenances(): List<VehicleMaintenanceEntity> {
        syncPendingMaintenances()
        safeApiCall { api.getMaintenances() }.onSuccess { list ->
            dao.upsertMaintenances(list.map {
                VehicleMaintenanceEntity(it.id, it.vehicleId, it.vehiclePlate, it.maintenanceType, it.description, it.workshop, it.cost, it.odometerKm, it.performedAt, it.nextDueDate, it.status, false)
            })
        }
        return dao.getMaintenances()
    }

    suspend fun addMaintenance(plate: String, type: String, description: String, workshop: String, cost: Double, odometerKm: Int): VehicleMaintenanceEntity {
        val localId = "local_${UUID.randomUUID()}"
        val local = VehicleMaintenanceEntity(localId, "", plate.trim(), type.trim(), description.trim(), workshop.trim(), cost, odometerKm, todayIso(), "", "COMPLETED", true)
        dao.upsertMaintenance(local)
        safeApiCall { api.createMaintenance(CreateMaintenanceRequestDto(null, plate.trim(), type.trim(), description.trim(), workshop.trim(), cost, odometerKm, todayIso(), null)) }
            .onSuccess { remote ->
                dao.deleteMaintenance(localId)
                val synced = VehicleMaintenanceEntity(remote.id, remote.vehicleId, remote.vehiclePlate, remote.maintenanceType, remote.description, remote.workshop, remote.cost, remote.odometerKm, remote.performedAt, remote.nextDueDate, remote.status, false)
                dao.upsertMaintenance(synced)
                return synced
            }
        return local
    }

    // ---- US-26: Operational Expenses ----

    suspend fun getExpenses(): List<OperationalExpenseEntity> {
        syncPendingExpenses()
        safeApiCall { api.getExpenses() }.onSuccess { list ->
            dao.upsertExpenses(list.map {
                OperationalExpenseEntity(it.id, it.vehicleId, it.vehiclePlate, it.driverName, it.category, it.description, it.amount, it.expenseDate, false)
            })
        }
        return dao.getExpenses()
    }

    suspend fun addExpense(plate: String, category: String, description: String, amount: Double): OperationalExpenseEntity {
        val localId = "local_${UUID.randomUUID()}"
        val driverName = sessionManager.current()?.fullName ?: ""
        val local = OperationalExpenseEntity(localId, "", plate.trim(), driverName, category.trim().uppercase(Locale.US), description.trim(), amount, todayIso(), true)
        dao.upsertExpense(local)
        safeApiCall { api.createExpense(CreateExpenseRequestDto(null, plate.trim(), category.trim().uppercase(Locale.US), description.trim(), amount, todayIso())) }
            .onSuccess { remote ->
                dao.deleteExpense(localId)
                val synced = OperationalExpenseEntity(remote.id, remote.vehicleId, remote.vehiclePlate, remote.driverName, remote.category, remote.description, remote.amount, remote.expenseDate, false)
                dao.upsertExpense(synced)
                return synced
            }
        return local
    }

    // ---- US-24: Driver Announcements ----

    suspend fun getAnnouncements(): List<RouteAnnouncementEntity> {
        syncPendingAnnouncements()
        safeApiCall { api.getAnnouncements() }.onSuccess { list ->
            dao.upsertAnnouncements(list.map {
                RouteAnnouncementEntity(it.id, it.routeId, it.authorName, it.title, it.message, it.priority, it.createdAt, false)
            })
        }
        return dao.getAnnouncements()
    }

    suspend fun publishAnnouncement(routeId: String?, title: String, message: String, priority: String = "NORMAL"): RouteAnnouncementEntity {
        val localId = "local_${UUID.randomUUID()}"
        val author = sessionManager.current()?.fullName ?: ""
        val local = RouteAnnouncementEntity(localId, routeId.orEmpty(), author, title.trim(), message.trim(), priority, nowIso(), true)
        dao.upsertAnnouncement(local)
        safeApiCall { api.createAnnouncement(CreateAnnouncementRequestDto(routeId, title.trim(), message.trim(), priority)) }
            .onSuccess { remote ->
                dao.deleteAnnouncement(localId)
                val synced = RouteAnnouncementEntity(remote.id, remote.routeId, remote.authorName, remote.title, remote.message, remote.priority, remote.createdAt, false)
                dao.upsertAnnouncement(synced)
                return synced
            }
        return local
    }

    // ---- US-27: Temporary Pickup Address Changes ----

    suspend fun getPickupChanges(): List<PickupChangeEntity> {
        syncPendingPickupChanges()
        safeApiCall { api.getPickupChanges() }.onSuccess { list ->
            dao.upsertPickupChanges(list.map {
                PickupChangeEntity(it.id, it.childId, it.childName, it.routeId, it.targetDate, it.newAddress, it.reason, it.status, it.createdAt, false)
            })
        }
        return dao.getPickupChanges()
    }

    suspend fun requestPickupChange(childId: String?, childName: String, routeId: String?, targetDate: String, newAddress: String, reason: String): PickupChangeEntity {
        val localId = "local_${UUID.randomUUID()}"
        val resolvedDate = targetDate.ifBlank { todayIso() }
        val local = PickupChangeEntity(localId, childId.orEmpty(), childName.trim(), routeId.orEmpty(), resolvedDate, newAddress.trim(), reason.trim(), "PENDING", nowIso(), true)
        dao.upsertPickupChange(local)
        safeApiCall { api.createPickupChange(CreatePickupChangeRequestDto(childId, childName.trim(), routeId, resolvedDate, newAddress.trim(), reason.trim())) }
            .onSuccess { remote ->
                dao.deletePickupChange(localId)
                val synced = PickupChangeEntity(remote.id, remote.childId, remote.childName, remote.routeId, remote.targetDate, remote.newAddress, remote.reason, remote.status, remote.createdAt, false)
                dao.upsertPickupChange(synced)
                return synced
            }
        return local
    }

    suspend fun updatePickupChangeStatus(requestId: String, status: String) {
        safeApiCall { api.updatePickupChangeStatus(requestId, UpdatePickupChangeStatusDto(status)) }.onSuccess { remote ->
            dao.upsertPickupChange(PickupChangeEntity(remote.id, remote.childId, remote.childName, remote.routeId, remote.targetDate, remote.newAddress, remote.reason, remote.status, remote.createdAt, false))
        }
    }

    // ---- US-30: Internal Driver-Parent Chat ----

    suspend fun getChatMessages(routeId: String? = null): List<ChatMessageEntity> {
        syncPendingChat()
        safeApiCall { api.getChatMessages(routeId) }.onSuccess { list ->
            dao.upsertChatMessages(list.map {
                ChatMessageEntity(it.id, it.routeId, it.senderUserId, it.senderName, it.senderRole, it.message, it.sentAt, false)
            })
        }
        return dao.getChatMessages()
    }

    suspend fun sendChatMessage(routeId: String?, message: String): ChatMessageEntity {
        val localId = "local_${UUID.randomUUID()}"
        val session = sessionManager.current()
        val local = ChatMessageEntity(localId, routeId.orEmpty(), session?.userId.orEmpty(), session?.fullName.orEmpty(), session?.role ?: "PARENT", message.trim(), nowIso(), true)
        dao.upsertChatMessage(local)
        safeApiCall { api.sendChatMessage(SendChatRequestDto(routeId, message.trim())) }
            .onSuccess { remote ->
                dao.deleteChatMessage(localId)
                val synced = ChatMessageEntity(remote.id, remote.routeId, remote.senderUserId, remote.senderName, remote.senderRole, remote.message, remote.sentAt, false)
                dao.upsertChatMessage(synced)
                return synced
            }
        return local
    }

    // ---- Background offline sync helpers ----

    suspend fun syncAllPending() {
        for (avatar in dao.pendingAvatars()) {
            safeApiCall { api.updateMyPhoto(UpdatePhotoRequestDto(avatar.photoData)) }.onSuccess {
                dao.upsertAvatar(avatar.copy(pendingSync = false))
            }
        }
        syncPendingCaregivers()
        syncPendingTickets()
        for (rating in dao.pendingRatings()) {
            safeApiCall { api.rateTrip(rating.tripId, RateTripRequestDto(rating.stars, rating.comment)) }.onSuccess {
                dao.upsertTripRating(rating.copy(pendingSync = false))
            }
        }
        syncPendingAbsences()
        syncPendingMaintenances()
        syncPendingExpenses()
        syncPendingAnnouncements()
        syncPendingPickupChanges()
        syncPendingChat()
    }

    private suspend fun syncPendingCaregivers() {
        for (item in dao.pendingCaregivers()) {
            safeApiCall { api.addCaregiver(CreateCaregiverRequestDto(item.fullName, item.relationship, item.phoneNumber)) }.onSuccess { remote ->
                dao.deleteCaregiver(item.id)
                dao.upsertCaregiver(item.copy(id = remote.id, pendingSync = false))
            }
        }
    }

    private suspend fun syncPendingTickets() {
        for (item in dao.pendingTickets()) {
            safeApiCall { api.createSupportTicket(CreateSupportTicketRequestDto(item.category, item.subject, item.message)) }.onSuccess { remote ->
                dao.deleteSupportTicket(item.id)
                dao.upsertSupportTicket(item.copy(id = remote.id, ticketCode = remote.ticketCode, pendingSync = false))
            }
        }
    }

    private suspend fun syncPendingAbsences() {
        for (item in dao.pendingAbsences()) {
            safeApiCall { api.createAbsence(CreateAbsenceRequestDto(item.childId.ifBlank { null }, item.childName, item.routeId.ifBlank { null }, item.absenceDate, item.reason, item.notes)) }.onSuccess { remote ->
                dao.deleteAbsence(item.id)
                dao.upsertAbsence(item.copy(id = remote.id, pendingSync = false))
            }
        }
    }

    private suspend fun syncPendingMaintenances() {
        for (item in dao.pendingMaintenances()) {
            safeApiCall { api.createMaintenance(CreateMaintenanceRequestDto(null, item.vehiclePlate, item.maintenanceType, item.description, item.workshop, item.cost, item.odometerKm, item.performedAt, null)) }.onSuccess { remote ->
                dao.deleteMaintenance(item.id)
                dao.upsertMaintenance(item.copy(id = remote.id, pendingSync = false))
            }
        }
    }

    private suspend fun syncPendingExpenses() {
        for (item in dao.pendingExpenses()) {
            safeApiCall { api.createExpense(CreateExpenseRequestDto(null, item.vehiclePlate, item.category, item.description, item.amount, item.expenseDate)) }.onSuccess { remote ->
                dao.deleteExpense(item.id)
                dao.upsertExpense(item.copy(id = remote.id, pendingSync = false))
            }
        }
    }

    private suspend fun syncPendingAnnouncements() {
        for (item in dao.pendingAnnouncements()) {
            safeApiCall { api.createAnnouncement(CreateAnnouncementRequestDto(item.routeId.ifBlank { null }, item.title, item.message, item.priority)) }.onSuccess { remote ->
                dao.deleteAnnouncement(item.id)
                dao.upsertAnnouncement(item.copy(id = remote.id, pendingSync = false))
            }
        }
    }

    private suspend fun syncPendingPickupChanges() {
        for (item in dao.pendingPickupChanges()) {
            safeApiCall { api.createPickupChange(CreatePickupChangeRequestDto(item.childId.ifBlank { null }, item.childName, item.routeId.ifBlank { null }, item.targetDate, item.newAddress, item.reason)) }.onSuccess { remote ->
                dao.deletePickupChange(item.id)
                dao.upsertPickupChange(item.copy(id = remote.id, pendingSync = false))
            }
        }
    }

    private suspend fun syncPendingChat() {
        for (item in dao.pendingChatMessages()) {
            safeApiCall { api.sendChatMessage(SendChatRequestDto(item.routeId.ifBlank { null }, item.message)) }.onSuccess { remote ->
                dao.deleteChatMessage(item.id)
                dao.upsertChatMessage(item.copy(id = remote.id, pendingSync = false))
            }
        }
    }
}
