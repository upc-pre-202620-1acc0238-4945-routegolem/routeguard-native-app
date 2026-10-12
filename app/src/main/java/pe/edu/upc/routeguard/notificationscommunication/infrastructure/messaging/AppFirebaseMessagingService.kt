package pe.edu.upc.routeguard.notificationscommunication.infrastructure.messaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.notificationscommunication.application.HandlePushMessageUseCase
import pe.edu.upc.routeguard.notificationscommunication.domain.PushPayload

class AppFirebaseMessagingService : FirebaseMessagingService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FirebaseServiceEntryPoint {
        fun handlePushMessageUseCase(): HandlePushMessageUseCase
    }

    private lateinit var handlePushMessageUseCase: HandlePushMessageUseCase

    override fun onCreate() {
        super.onCreate()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            FirebaseServiceEntryPoint::class.java
        )
        handlePushMessageUseCase = entryPoint.handlePushMessageUseCase()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: ""
        val body = message.notification?.body ?: ""
        val type = message.data["type"] ?: ""

        val payload = PushPayload(
            type = type,
            title = title,
            body = body,
            data = message.data
        )

        CoroutineScope(Dispatchers.IO).launch {
            handlePushMessageUseCase(payload)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
