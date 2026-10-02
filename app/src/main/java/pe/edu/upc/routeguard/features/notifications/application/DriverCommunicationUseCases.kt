package pe.edu.upc.routeguard.features.notifications.application

import pe.edu.upc.routeguard.features.notifications.domain.NotificationRepository
import javax.inject.Inject

/** Trigger Panic Alert: high priority alert for the parents of the route being driven. */
class TriggerPanicAlertUseCase @Inject constructor(private val repository: NotificationRepository) {
    suspend operator fun invoke(tripId: String?): Result<Int> {
        if (tripId.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Inicia un viaje para enviar una alerta"))
        }
        return repository.triggerPanicAlert(tripId)
    }
}

/** Post Broadcast Message: announcement from the driver to the parents. */
class PostBroadcastMessageUseCase @Inject constructor(private val repository: NotificationRepository) {
    suspend operator fun invoke(tripId: String?, message: String): Result<Int> {
        if (tripId.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Inicia un viaje para enviar un aviso"))
        }
        if (message.isBlank()) return Result.failure(IllegalArgumentException("Escribe un mensaje"))
        return repository.postBroadcastMessage(tripId, message.trim())
    }
}
