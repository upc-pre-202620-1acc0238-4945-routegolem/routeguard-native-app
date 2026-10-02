package pe.edu.upc.routeguard.features.notifications.application

import pe.edu.upc.routeguard.features.notifications.domain.GeofenceAlert
import pe.edu.upc.routeguard.features.notifications.domain.GeofenceAlertType
import pe.edu.upc.routeguard.features.notifications.domain.Notification
import pe.edu.upc.routeguard.features.notifications.domain.NotificationStatus
import pe.edu.upc.routeguard.features.notifications.domain.NotificationType
import pe.edu.upc.routeguard.features.notifications.domain.PushPayload
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
        val id = payload.data["notificationId"] ?: UUID.randomUUID().toString()
        val tripId = payload.data["tripId"].orEmpty()

        val notification = if (type == NotificationType.GEOFENCE_BREACHED) {
            val alertType = if (payload.data["alertType"] == GeofenceAlertType.ARRIVED_AT_SCHOOL.name) {
                GeofenceAlertType.ARRIVED_AT_SCHOOL
            } else {
                GeofenceAlertType.APPROACHING_STOP
            }
            processGeofence(GeofenceAlert(id, alertType, null, payload.body, now), tripId)
        } else {
            Notification(
                id = id,
                tripId = tripId,
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
