package pe.edu.upc.routeguard.notificationscommunication.infrastructure.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
@JvmSuppressWildcards
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY created_at DESC")
    suspend fun fetchAllNotifications(): List<NotificationEntity>

    @Query("DELETE FROM notifications")
    suspend fun deleteAll(): Int

    @Upsert
    suspend fun upsertNotifications(entities: List<NotificationEntity>): List<Long>

    @JvmSuppressWildcards
    @Query("UPDATE notifications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String): Int
}
