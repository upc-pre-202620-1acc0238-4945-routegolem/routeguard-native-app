package pe.edu.upc.routeguard.notificationscommunication.infrastructure.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.PushNotificationAdapter
import javax.inject.Inject

/**
 * Push adapter. It already shows notifications in the system tray; receiving pushes from FCM
 * needs the Firebase SDK + google-services.json and a service that calls HandlePushMessageUseCase.
 */
class FcmPushAdapter @Inject constructor(
    @ApplicationContext private val context: Context
) : PushNotificationAdapter {

    override fun show(notification: Notification) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val channelId = if (notification.isHighPriority) CHANNEL_HIGH else CHANNEL_DEFAULT
        createChannels()

        val built = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setPriority(
                if (notification.isHighPriority) NotificationCompat.PRIORITY_MAX
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .build()

        try {
            manager.notify(notification.id.hashCode(), built)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted: the notification stays in the in-app list.
        }
    }

    private fun createChannels() {
        val system = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        system.createNotificationChannel(
            NotificationChannel(CHANNEL_DEFAULT, "Avisos del viaje", NotificationManager.IMPORTANCE_DEFAULT)
        )
        system.createNotificationChannel(
            NotificationChannel(CHANNEL_HIGH, "Alertas de alta prioridad", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    private companion object {
        const val CHANNEL_DEFAULT = "trip_notifications"
        const val CHANNEL_HIGH = "high_priority_alerts"
    }
}
