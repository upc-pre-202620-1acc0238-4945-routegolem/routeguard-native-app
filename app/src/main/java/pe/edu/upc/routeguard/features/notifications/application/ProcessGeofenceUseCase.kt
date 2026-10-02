package pe.edu.upc.routeguard.features.notifications.application

import pe.edu.upc.routeguard.features.notifications.domain.GeofenceAlert
import pe.edu.upc.routeguard.features.notifications.domain.GeofenceAlertType
import pe.edu.upc.routeguard.features.notifications.domain.Notification
import pe.edu.upc.routeguard.features.notifications.domain.NotificationRepository
import pe.edu.upc.routeguard.features.notifications.domain.NotificationStatus
import pe.edu.upc.routeguard.features.notifications.domain.NotificationType
import javax.inject.Inject

/** Geofence Breached -> Notification Created. */
class ProcessGeofenceUseCase @Inject constructor(private val repository: NotificationRepository) {

    suspend operator fun invoke(alert: GeofenceAlert, tripId: String = ""): Notification {
        val title = when (alert.alertType) {
            GeofenceAlertType.APPROACHING_STOP -> "El vehículo está cerca"
            GeofenceAlertType.ARRIVED_AT_SCHOOL -> "Llegada al colegio"
        }
        val notification = Notification(
            id = alert.id,
            tripId = tripId,
            type = NotificationType.GEOFENCE_BREACHED,
            status = NotificationStatus.PENDING,
            title = title,
            body = alert.message,
            createdAt = alert.triggeredAt,
            hasPanicAlert = false
        )
        repository.save(notification)
        return notification
    }
}
