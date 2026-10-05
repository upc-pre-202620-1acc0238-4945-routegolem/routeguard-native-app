package pe.edu.upc.routeguard.notifications.infrastructure.remote

data class AlertDto(
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["alertId", "alert_id"])
    val id: String,
    val panic: Boolean
)

data class NotificationDto(
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["notificationId", "notification_id"])
    val id: pe.edu.upc.routeguard.notifications.domain.valueobject.NotificationId,
    @com.google.gson.annotations.SerializedName(value = "tripId", alternate = ["trip_id"])
    val tripId: pe.edu.upc.routeguard.trip.domain.valueobject.TripId?,
    val category: String?,
    val deliveryState: String?,
    val message: String?,
    val sentAt: String?,
    val alerts: List<AlertDto>?
)

data class BroadcastRequestDto(val message: String)

data class FanOutResponseDto(val notified: Int)
