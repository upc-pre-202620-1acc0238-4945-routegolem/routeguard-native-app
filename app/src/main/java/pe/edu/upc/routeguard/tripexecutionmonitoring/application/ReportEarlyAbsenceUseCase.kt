package pe.edu.upc.routeguard.tripexecutionmonitoring.application

import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationRepository
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationStatus
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationType
import pe.edu.upc.routeguard.notificationscommunication.domain.valueobject.NotificationId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripRepository
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.StudentId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.TripId
import java.util.UUID
import javax.inject.Inject

/** Logs a parent's early absence report for today and persists a local notification (US-09). */
class ReportEarlyAbsenceUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        tripId: TripId?,
        studentId: StudentId,
        studentName: String,
        reason: String
    ): Result<Unit> {
        val result = tripRepository.reportEarlyAbsence(tripId, studentId, studentName, reason)
        notificationRepository.save(
            Notification(
                id = NotificationId("absence_${UUID.randomUUID()}"),
                tripId = tripId ?: TripId("trp_01"),
                type = NotificationType.ANNOUNCEMENT,
                status = NotificationStatus.DISPATCHED,
                title = "Ausencia temprana reportada",
                body = "$studentName no asistirá hoy ($reason).",
                createdAt = System.currentTimeMillis(),
                hasPanicAlert = false
            )
        )
        return result
    }
}
