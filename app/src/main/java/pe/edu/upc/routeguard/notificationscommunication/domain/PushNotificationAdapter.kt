package pe.edu.upc.routeguard.notificationscommunication.domain

/** Port for the push provider (FCM) and the device notification tray. */
interface PushNotificationAdapter {
    /** Shows the notification on the device. */
    fun show(notification: Notification)
}
