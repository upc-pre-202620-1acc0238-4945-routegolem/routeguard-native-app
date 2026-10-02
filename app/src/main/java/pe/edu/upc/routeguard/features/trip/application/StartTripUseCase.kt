package pe.edu.upc.routeguard.features.trip.application

import pe.edu.upc.routeguard.features.trip.domain.Trip
import pe.edu.upc.routeguard.features.trip.domain.TripRepository
import javax.inject.Inject

class StartTripUseCase @Inject constructor(private val repository: TripRepository) {

    suspend operator fun invoke(routeId: String): Result<Trip> {
        if (repository.getActiveTrip() != null) {
            return Result.failure(IllegalStateException("Ya tienes un viaje en curso"))
        }
        return repository.startTrip(routeId)
    }
}
