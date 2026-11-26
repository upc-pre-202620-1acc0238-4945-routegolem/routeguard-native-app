package pe.edu.upc.routeguard.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
@JvmSuppressWildcards
interface PlatformCacheDao {

    @Query("SELECT * FROM user_avatar_cache WHERE userId = :userId LIMIT 1")
    suspend fun getAvatar(userId: String): UserAvatarCacheEntity?

    @Upsert
    suspend fun upsertAvatar(entity: UserAvatarCacheEntity): Long

    @Query("SELECT * FROM user_avatar_cache WHERE pendingSync = 1")
    suspend fun pendingAvatars(): List<UserAvatarCacheEntity>

    @Query("SELECT * FROM vehicle_docs_cache ORDER BY plate")
    suspend fun getVehicles(): List<VehicleDocsCacheEntity>

    @Upsert
    suspend fun upsertVehicles(items: List<VehicleDocsCacheEntity>): List<Long>

    @Query("SELECT * FROM authorized_caregivers ORDER BY createdAt")
    suspend fun getCaregivers(): List<AuthorizedCaregiverEntity>

    @Upsert
    suspend fun upsertCaregivers(items: List<AuthorizedCaregiverEntity>): List<Long>

    @Upsert
    suspend fun upsertCaregiver(item: AuthorizedCaregiverEntity): Long

    @Query("DELETE FROM authorized_caregivers WHERE id = :id")
    suspend fun deleteCaregiver(id: String): Int

    @Query("DELETE FROM authorized_caregivers WHERE pendingSync = 0")
    suspend fun clearSyncedCaregivers(): Int

    @Query("SELECT * FROM authorized_caregivers WHERE pendingSync = 1")
    suspend fun pendingCaregivers(): List<AuthorizedCaregiverEntity>

    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    suspend fun getSupportTickets(): List<SupportTicketEntity>

    @Upsert
    suspend fun upsertSupportTickets(items: List<SupportTicketEntity>): List<Long>

    @Upsert
    suspend fun upsertSupportTicket(item: SupportTicketEntity): Long

    @Query("DELETE FROM support_tickets WHERE id = :id")
    suspend fun deleteSupportTicket(id: String): Int

    @Query("SELECT * FROM support_tickets WHERE pendingSync = 1")
    suspend fun pendingTickets(): List<SupportTicketEntity>

    @Query("SELECT * FROM trip_ratings WHERE tripId = :tripId LIMIT 1")
    suspend fun getTripRating(tripId: String): TripRatingEntity?

    @Upsert
    suspend fun upsertTripRating(item: TripRatingEntity): Long

    @Query("SELECT * FROM trip_ratings WHERE pendingSync = 1")
    suspend fun pendingRatings(): List<TripRatingEntity>

    @Query("SELECT * FROM absence_reports ORDER BY createdAt DESC")
    suspend fun getAbsences(): List<AbsenceReportEntity>

    @Upsert
    suspend fun upsertAbsences(items: List<AbsenceReportEntity>): List<Long>

    @Upsert
    suspend fun upsertAbsence(item: AbsenceReportEntity): Long

    @Query("DELETE FROM absence_reports WHERE id = :id")
    suspend fun deleteAbsence(id: String): Int

    @Query("SELECT * FROM absence_reports WHERE pendingSync = 1")
    suspend fun pendingAbsences(): List<AbsenceReportEntity>

    @Query("SELECT * FROM vehicle_maintenances ORDER BY performedAt DESC")
    suspend fun getMaintenances(): List<VehicleMaintenanceEntity>

    @Upsert
    suspend fun upsertMaintenances(items: List<VehicleMaintenanceEntity>): List<Long>

    @Upsert
    suspend fun upsertMaintenance(item: VehicleMaintenanceEntity): Long

    @Query("DELETE FROM vehicle_maintenances WHERE id = :id")
    suspend fun deleteMaintenance(id: String): Int

    @Query("SELECT * FROM vehicle_maintenances WHERE pendingSync = 1")
    suspend fun pendingMaintenances(): List<VehicleMaintenanceEntity>

    @Query("SELECT * FROM operational_expenses ORDER BY expenseDate DESC")
    suspend fun getExpenses(): List<OperationalExpenseEntity>

    @Upsert
    suspend fun upsertExpenses(items: List<OperationalExpenseEntity>): List<Long>

    @Upsert
    suspend fun upsertExpense(item: OperationalExpenseEntity): Long

    @Query("DELETE FROM operational_expenses WHERE id = :id")
    suspend fun deleteExpense(id: String): Int

    @Query("SELECT * FROM operational_expenses WHERE pendingSync = 1")
    suspend fun pendingExpenses(): List<OperationalExpenseEntity>

    @Query("SELECT * FROM route_announcements ORDER BY createdAt DESC")
    suspend fun getAnnouncements(): List<RouteAnnouncementEntity>

    @Upsert
    suspend fun upsertAnnouncements(items: List<RouteAnnouncementEntity>): List<Long>

    @Upsert
    suspend fun upsertAnnouncement(item: RouteAnnouncementEntity): Long

    @Query("DELETE FROM route_announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: String): Int

    @Query("SELECT * FROM route_announcements WHERE pendingSync = 1")
    suspend fun pendingAnnouncements(): List<RouteAnnouncementEntity>

    @Query("SELECT * FROM pickup_change_requests ORDER BY createdAt DESC")
    suspend fun getPickupChanges(): List<PickupChangeEntity>

    @Upsert
    suspend fun upsertPickupChanges(items: List<PickupChangeEntity>): List<Long>

    @Upsert
    suspend fun upsertPickupChange(item: PickupChangeEntity): Long

    @Query("DELETE FROM pickup_change_requests WHERE id = :id")
    suspend fun deletePickupChange(id: String): Int

    @Query("SELECT * FROM pickup_change_requests WHERE pendingSync = 1")
    suspend fun pendingPickupChanges(): List<PickupChangeEntity>

    @Query("SELECT * FROM chat_messages ORDER BY sent_at ASC")
    suspend fun getChatMessages(): List<ChatMessageEntity>

    @Upsert
    suspend fun upsertChatMessages(items: List<ChatMessageEntity>): List<Long>

    @Upsert
    suspend fun upsertChatMessage(item: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteChatMessage(id: String): Int

    @Query("SELECT * FROM chat_messages WHERE pendingSync = 1")
    suspend fun pendingChatMessages(): List<ChatMessageEntity>
}
