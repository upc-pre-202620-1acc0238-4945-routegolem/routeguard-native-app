package pe.edu.upc.routeguard.notificationscommunication.application

import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.localization.LocalizedException
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationRepository
import javax.inject.Inject

/** Trigger Panic Alert: high priority alert for the parents of the route being driven. */
class TriggerPanicAlertUseCase @Inject constructor(private val repository: NotificationRepository) {
    suspend operator fun invoke(tripId: String?): Result<Int> {
        if (tripId.isNullOrBlank()) {
            return Result.failure(LocalizedException(R.string.notif_error_start_trip_alert, fallback = "Inicia un viaje para enviar una alerta"))
        }
        return repository.triggerPanicAlert(tripId)
    }
}

/** Post Broadcast Message: announcement from the driver to the parents. */
class PostBroadcastMessageUseCase @Inject constructor(private val repository: NotificationRepository) {
    suspend operator fun invoke(tripId: String?, message: String): Result<Int> {
        if (tripId.isNullOrBlank()) {
            return Result.failure(LocalizedException(R.string.notif_error_start_trip_notice, fallback = "Inicia un viaje para enviar un aviso"))
        }
        if (message.isBlank()) return Result.failure(LocalizedException(R.string.notif_error_empty_msg, fallback = "Escribe un mensaje"))
        return repository.postBroadcastMessage(tripId, message.trim())
    }
}
