package pe.edu.upc.routeguard.notificationscommunication.presentation.alerts

import pe.edu.upc.routeguard.notificationscommunication.domain.Notification

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val broadcastMessage: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)
