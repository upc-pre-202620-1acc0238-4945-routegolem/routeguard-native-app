package pe.edu.upc.routeguard.notifications.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.safeApiCall
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.notifications.domain.Notification
import pe.edu.upc.routeguard.notifications.domain.NotificationRepository
import pe.edu.upc.routeguard.notifications.domain.NotificationStatus
import pe.edu.upc.routeguard.notifications.infrastructure.local.NotificationDao
import pe.edu.upc.routeguard.notifications.infrastructure.remote.BroadcastRequestDto
import pe.edu.upc.routeguard.notifications.infrastructure.remote.NotificationApiService
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val service: NotificationApiService,
    private val dao: NotificationDao,
    private val sessionManager: SessionManager
) : NotificationRepository {

    /** Parents only see their own notifications; drivers see the ones of their organization. */
    override suspend fun getNotifications(): List<Notification> {
        val session = sessionManager.current()
        val parentId = if (session?.role == "PARENT") session.profileId else null

        safeApiCall { service.getNotifications(parentId) }
            .onSuccess { dtos -> dao.upsertNotifications(dtos.map { it.toEntity() }) }

        val local = dao.fetchAllNotifications().map { it.toDomain() }
        return local
    }

    override suspend fun save(notification: Notification) {
        dao.upsertNotifications(listOf(notification.toEntity()))
    }

    override suspend fun markDelivered(notificationId: String): Result<Unit> =
        safeApiCall { service.markDelivered(notificationId) }
            .map { dao.updateStatus(notificationId, NotificationStatus.DELIVERED.name) }

    override suspend fun triggerPanicAlert(tripId: String): Result<Int> =
        safeApiCall { service.triggerPanicAlert(tripId) }.map { it.notified }

    override suspend fun postBroadcastMessage(tripId: String, message: String): Result<Int> =
        safeApiCall { service.postBroadcastMessage(tripId, BroadcastRequestDto(message)) }.map { it.notified }
}
