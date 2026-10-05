package pe.edu.upc.routeguard.notifications.application

import pe.edu.upc.routeguard.notifications.domain.Notification
import pe.edu.upc.routeguard.notifications.domain.NotificationRepository
import javax.inject.Inject

/** High priority notifications go first, then the most recent ones. */
class GetNotificationsUseCase @Inject constructor(private val repository: NotificationRepository) {

    suspend operator fun invoke(): List<Notification> =
        repository.getNotifications()
            .sortedWith(compareByDescending<Notification> { it.isHighPriority }.thenByDescending { it.createdAt })
}

/** Notification Delivered: the parent has the notification on the device. */
class MarkNotificationsDeliveredUseCase @Inject constructor(private val repository: NotificationRepository) {

    suspend operator fun invoke(notifications: List<Notification>) {
        notifications.filter { !it.isDelivered }.forEach { repository.markDelivered(it.id.value) }
    }
}
