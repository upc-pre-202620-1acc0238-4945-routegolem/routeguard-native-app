package pe.edu.upc.routeguard.notifications.application

import pe.edu.upc.routeguard.notifications.domain.Notification
import pe.edu.upc.routeguard.notifications.domain.NotificationRepository
import pe.edu.upc.routeguard.notifications.domain.NotificationStatus
import pe.edu.upc.routeguard.notifications.domain.PushNotificationAdapter
import javax.inject.Inject

/** Notification Dispatched: shows the alert on the device and records it as dispatched. */
class DispatchAlertUseCase @Inject constructor(
    private val repository: NotificationRepository,
    private val pushAdapter: PushNotificationAdapter
) {
    suspend operator fun invoke(notification: Notification): Notification {
        val dispatched = notification.copy(status = NotificationStatus.DISPATCHED)
        pushAdapter.show(dispatched)
        repository.save(dispatched)
        return dispatched
    }
}
