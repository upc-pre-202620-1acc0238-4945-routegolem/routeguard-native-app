package pe.edu.upc.routeguard.features.notifications.presentation.alerts

import pe.edu.upc.routeguard.features.notifications.domain.Notification

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val broadcastMessage: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)
