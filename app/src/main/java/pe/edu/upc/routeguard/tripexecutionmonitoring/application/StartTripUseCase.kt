package pe.edu.upc.routeguard.tripexecutionmonitoring.application

import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Trip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripRepository
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.RouteId
import javax.inject.Inject

class StartTripUseCase @Inject constructor(private val repository: TripRepository) {

    suspend operator fun invoke(routeId: RouteId): Result<Trip> {
        if (repository.getActiveTrip() != null) {
            return Result.failure(IllegalStateException("Ya tienes un viaje en curso"))
        }
        return repository.startTrip(routeId)
    }
}
