package pe.edu.upc.routeguard.features.trip.application

import pe.edu.upc.routeguard.features.trip.domain.BoardingState
import pe.edu.upc.routeguard.features.trip.domain.Trip
import pe.edu.upc.routeguard.features.trip.domain.TripRepository
import javax.inject.Inject

/** Set Boarding Status: Student Boarded / absent with one touch. */
class BoardStudentUseCase @Inject constructor(private val repository: TripRepository) {

    suspend operator fun invoke(trip: Trip, studentId: String, state: BoardingState): Result<Trip> {
        if (!trip.acceptsRecords) {
            return Result.failure(IllegalStateException("El viaje no está en curso"))
        }
        val waypoint = trip.waypoints.firstOrNull { it.studentId == studentId }
            ?: return Result.failure(IllegalArgumentException("El estudiante no pertenece al viaje"))
        if (state == BoardingState.MISSING || waypoint.status == state) {
            return Result.failure(IllegalStateException("Estado de abordaje no válido para este estudiante"))
        }
        return repository.setBoardingStatus(trip.id, studentId, state)
    }
}
