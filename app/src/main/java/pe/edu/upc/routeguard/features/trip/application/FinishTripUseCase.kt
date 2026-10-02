package pe.edu.upc.routeguard.features.trip.application

import pe.edu.upc.routeguard.features.trip.domain.Trip
import pe.edu.upc.routeguard.features.trip.domain.TripRepository
import javax.inject.Inject

/** Complete Trip: a completed trip no longer accepts records. */
class FinishTripUseCase @Inject constructor(
    private val repository: TripRepository,
    private val syncOfflineRecords: SyncOfflineRecordsUseCase
) {

    suspend operator fun invoke(trip: Trip): Result<Trip> {
        if (!trip.isActive) {
            return Result.failure(IllegalStateException("El viaje ya fue finalizado"))
        }
        // Deliver whatever is pending first; a failure here does not block the completion.
        syncOfflineRecords(trip.id)
        return repository.completeTrip(trip.id)
    }
}
