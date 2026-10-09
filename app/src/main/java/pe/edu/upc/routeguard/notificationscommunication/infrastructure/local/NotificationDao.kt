package pe.edu.upc.routeguard.notificationscommunication.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY created_at DESC")
    suspend fun fetchAllNotifications(): List<NotificationEntity>

    @Upsert
    suspend fun upsertNotifications(entities: List<NotificationEntity>)

    @Query("UPDATE notifications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}
