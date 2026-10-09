package pe.edu.upc.routeguard.notificationscommunication.application

import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationRepository
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationStatus
import pe.edu.upc.routeguard.notificationscommunication.domain.PushNotificationAdapter
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
