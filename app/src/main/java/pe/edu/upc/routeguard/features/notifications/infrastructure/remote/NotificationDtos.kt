package pe.edu.upc.routeguard.features.notifications.infrastructure.remote

data class AlertDto(
    val id: String,
    val panic: Boolean
)

data class NotificationDto(
    val id: String,
    val tripId: String,
    val category: String,
    val deliveryState: String,
    val message: String,
    val sentAt: String?,
    val alerts: List<AlertDto>?
)

data class BroadcastRequestDto(val message: String)

data class FanOutResponseDto(val notified: Int)
