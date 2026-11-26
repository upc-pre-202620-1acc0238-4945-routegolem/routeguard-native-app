package pe.edu.upc.routeguard.tripexecutionmonitoring.application

import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.localization.LocalizedException
import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationRepository
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationStatus
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationType
import pe.edu.upc.routeguard.notificationscommunication.domain.valueobject.NotificationId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Trip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripRepository
import java.util.UUID
import javax.inject.Inject

/** Cancels an active trip due to force majeure, disables boarding, and dispatches a critical alert (US-21). */
class CancelTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val notificationRepository: NotificationRepository,
    private val syncOfflineRecords: SyncOfflineRecordsUseCase
) {
    suspend operator fun invoke(trip: Trip, reason: String): Result<Trip> {
        if (!trip.isActive) {
            return Result.failure(
                LocalizedException(R.string.tracking_error_not_in_progress, fallback = "El viaje no está en curso")
            )
        }
        val cleanReason = reason.trim().ifBlank { "Fuerza mayor" }
        syncOfflineRecords(trip.id)
        val result = tripRepository.cancelTrip(trip.id, cleanReason)
        result.onSuccess {
            val alert = Notification(
                id = NotificationId("cancel_${UUID.randomUUID()}"),
                tripId = trip.id,
                type = NotificationType.PANIC_ALERT,
                status = NotificationStatus.DISPATCHED,
                title = "Viaje cancelado por fuerza mayor",
                body = "${trip.routeName}: $cleanReason",
                createdAt = System.currentTimeMillis(),
                hasPanicAlert = true
            )
            notificationRepository.save(alert)
            notificationRepository.triggerPanicAlert(trip.id.value)
            notificationRepository.postBroadcastMessage(trip.id.value, "CANCELADO: $cleanReason")
        }
        return result
    }
}
