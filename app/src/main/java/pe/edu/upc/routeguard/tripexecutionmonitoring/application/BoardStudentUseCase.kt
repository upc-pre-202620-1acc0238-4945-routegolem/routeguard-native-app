package pe.edu.upc.routeguard.tripexecutionmonitoring.application

import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.localization.LocalizedException
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.BoardingState
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Trip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripRepository
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.StudentId
import javax.inject.Inject

/** Set Boarding Status: Student Boarded / absent with one touch. */
class BoardStudentUseCase @Inject constructor(private val repository: TripRepository) {

    suspend operator fun invoke(trip: Trip, studentId: StudentId, state: BoardingState): Result<Trip> {
        if (!trip.acceptsRecords) {
            return Result.failure(LocalizedException(R.string.tracking_error_not_in_progress, fallback = "El viaje no está en curso"))
        }
        val waypoint = trip.waypoints.firstOrNull { it.studentId == studentId }
            ?: return Result.failure(LocalizedException(R.string.tracking_error_student_not_in_trip, fallback = "El estudiante no pertenece al viaje"))
        if (state == BoardingState.MISSING || waypoint.status == state) {
            return Result.failure(LocalizedException(R.string.tracking_error_invalid_boarding, fallback = "Estado de abordaje no válido para este estudiante"))
        }
        return repository.setBoardingStatus(trip.id, studentId, state)
    }
}
