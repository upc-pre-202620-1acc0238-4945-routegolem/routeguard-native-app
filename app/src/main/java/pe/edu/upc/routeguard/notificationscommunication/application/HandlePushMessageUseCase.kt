package pe.edu.upc.routeguard.notificationscommunication.application

import pe.edu.upc.routeguard.notificationscommunication.domain.GeofenceAlert
import pe.edu.upc.routeguard.notificationscommunication.domain.GeofenceAlertType
import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationStatus
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationType
import pe.edu.upc.routeguard.notificationscommunication.domain.PushPayload
import pe.edu.upc.routeguard.notificationscommunication.domain.valueobject.GeofenceAlertId
import pe.edu.upc.routeguard.notificationscommunication.domain.valueobject.NotificationId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.TripId
import java.util.UUID
import javax.inject.Inject

/**
 * Entry point for every push received from the provider (FCM): geofence breaches are processed
 * as such, the rest become notifications. Everything ends up dispatched on the device.
 */
class HandlePushMessageUseCase @Inject constructor(
    private val processGeofence: ProcessGeofenceUseCase,
    private val dispatchAlert: DispatchAlertUseCase
) {
    suspend operator fun invoke(payload: PushPayload): Notification {
        val type = NotificationType.from(payload.type)
        val now = System.currentTimeMillis()
        val idString = payload.data["notificationId"] ?: UUID.randomUUID().toString()
        val tripIdString = payload.data["tripId"].orEmpty()

        val notification = if (type == NotificationType.GEOFENCE_BREACHED) {
            val alertType = if (payload.data["alertType"] == GeofenceAlertType.ARRIVED_AT_SCHOOL.name) {
                GeofenceAlertType.ARRIVED_AT_SCHOOL
            } else {
                GeofenceAlertType.APPROACHING_STOP
            }
            processGeofence(GeofenceAlert(GeofenceAlertId(idString), alertType, null, payload.body, now), TripId(tripIdString))
        } else {
            Notification(
                id = NotificationId(idString),
                tripId = TripId(tripIdString),
                type = type,
                status = NotificationStatus.PENDING,
                title = payload.title,
                body = payload.body,
                createdAt = now,
                hasPanicAlert = type == NotificationType.PANIC_ALERT
            )
        }
        return dispatchAlert(notification)
    }
}
