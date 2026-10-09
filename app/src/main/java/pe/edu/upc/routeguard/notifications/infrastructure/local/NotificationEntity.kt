package pe.edu.upc.routeguard.notifications.infrastructure.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "trip_id")
    val tripId: String,
    val type: String,
    val status: String,
    val title: String,
    val body: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "has_panic_alert")
    val hasPanicAlert: Boolean
)
