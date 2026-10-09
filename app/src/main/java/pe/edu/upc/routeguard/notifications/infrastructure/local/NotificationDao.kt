package pe.edu.upc.routeguard.notifications.infrastructure.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY created_at DESC")
    suspend fun fetchAllNotifications(): List<NotificationEntity>

    @Upsert
    suspend fun upsertNotifications(entities: List<NotificationEntity>)

    @Query("UPDATE notifications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}
