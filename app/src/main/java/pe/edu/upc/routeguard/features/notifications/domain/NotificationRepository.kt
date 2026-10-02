package pe.edu.upc.routeguard.features.notifications.domain

interface NotificationRepository {
    /** Refreshes from the backend and falls back to the local cache when offline. */
    suspend fun getNotifications(): List<Notification>

    suspend fun save(notification: Notification)

    suspend fun markDelivered(notificationId: String): Result<Unit>

    /** Panic alert of the driver: high priority notification to the parents of the trip. */
    suspend fun triggerPanicAlert(tripId: String): Result<Int>

    /** Announcement of the driver to the parents of the trip. */
    suspend fun postBroadcastMessage(tripId: String, message: String): Result<Int>
}
