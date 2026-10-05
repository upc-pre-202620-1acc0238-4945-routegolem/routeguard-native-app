package pe.edu.upc.routeguard.trip.application

import pe.edu.upc.routeguard.trip.domain.Trip
import pe.edu.upc.routeguard.trip.domain.TripRepository
import pe.edu.upc.routeguard.trip.domain.valueobject.RouteId
import javax.inject.Inject

class StartTripUseCase @Inject constructor(private val repository: TripRepository) {

    suspend operator fun invoke(routeId: RouteId): Result<Trip> {
        if (repository.getActiveTrip() != null) {
            return Result.failure(IllegalStateException("Ya tienes un viaje en curso"))
        }
        return repository.startTrip(routeId)
    }
}
