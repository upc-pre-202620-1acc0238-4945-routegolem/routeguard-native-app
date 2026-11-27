package pe.edu.upc.routeguard.notificationscommunication.application

import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationRepository
import javax.inject.Inject

/** High priority notifications go first, then the most recent ones. */
class GetNotificationsUseCase @Inject constructor(private val repository: NotificationRepository) {

    suspend operator fun invoke(): List<Notification> =
        repository.getNotifications()
            .sortedWith(compareByDescending<Notification> { it.isHighPriority }.thenByDescending { it.createdAt })
}

/** Notification Delivered: the parent has the notification on the device. Returns confirmed IDs. */
class MarkNotificationsDeliveredUseCase @Inject constructor(private val repository: NotificationRepository) {

    suspend operator fun invoke(notifications: List<Notification>): Set<String> =
        notifications
            .filter { !it.isDelivered }
            .mapNotNull { item ->
                if (repository.markDelivered(item.id.value).isSuccess) item.id.value else null
            }
            .toSet()
}
