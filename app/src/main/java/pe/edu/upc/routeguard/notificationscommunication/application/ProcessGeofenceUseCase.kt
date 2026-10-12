package pe.edu.upc.routeguard.notificationscommunication.application

import pe.edu.upc.routeguard.notificationscommunication.domain.GeofenceAlert
import pe.edu.upc.routeguard.notificationscommunication.domain.GeofenceAlertType
import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationRepository
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationStatus
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationType
import pe.edu.upc.routeguard.notificationscommunication.domain.valueobject.NotificationId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.TripId
import javax.inject.Inject

/** Geofence Breached -> Notification Created. */
class ProcessGeofenceUseCase @Inject constructor(private val repository: NotificationRepository) {

    suspend operator fun invoke(alert: GeofenceAlert, tripId: TripId): Notification {
        val title = when (alert.alertType) {
            GeofenceAlertType.APPROACHING_STOP -> "El vehículo está cerca"
            GeofenceAlertType.ARRIVED_AT_SCHOOL -> "Llegada al colegio"
        }
        val notification = Notification(
            id = NotificationId(alert.id.value),
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
