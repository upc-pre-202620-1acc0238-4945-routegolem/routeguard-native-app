package pe.edu.upc.routeguard.notificationscommunication.presentation.alerts

import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationType

enum class AlertFilter {
    ALL, PRIORITY, TRIP, NOTICES;

    fun matches(notification: Notification): Boolean = when (this) {
        ALL -> true
        PRIORITY -> notification.isHighPriority
        TRIP -> notification.type == NotificationType.TRIP_STARTED ||
            notification.type == NotificationType.STUDENT_BOARDED ||
            notification.type == NotificationType.GEOFENCE_BREACHED
        NOTICES -> notification.type == NotificationType.ANNOUNCEMENT ||
            notification.type == NotificationType.GENERAL
    }
}

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val selectedFilter: AlertFilter = AlertFilter.ALL,
    val broadcastMessage: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
) {
    val filteredNotifications: List<Notification>
        get() = notifications.filter { selectedFilter.matches(it) }
}
