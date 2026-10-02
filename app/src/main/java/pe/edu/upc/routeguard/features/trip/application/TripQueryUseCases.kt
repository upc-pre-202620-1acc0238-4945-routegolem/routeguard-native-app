package pe.edu.upc.routeguard.features.trip.application

import pe.edu.upc.routeguard.features.trip.domain.TripRepository
import javax.inject.Inject

/** Select Route Card: routes assigned to the driver and ready to run. */
class GetAssignedRoutesUseCase @Inject constructor(private val repository: TripRepository) {
    suspend operator fun invoke() = repository.getAssignedRoutes()
}

class GetActiveTripUseCase @Inject constructor(private val repository: TripRepository) {
    suspend operator fun invoke() = repository.getActiveTrip()
}

/** Administrator monitor: all the trips in progress. */
class GetLiveTripsUseCase @Inject constructor(private val repository: TripRepository) {
    suspend operator fun invoke() = repository.getLiveTrips()
}

/** Parent tracking: where is the vehicle that carries my children? */
class FollowChildTripUseCase @Inject constructor(private val repository: TripRepository) {
    suspend operator fun invoke() = repository.getLiveTripForParent()
}

/** Feeds the live position of the driver on the map. */
class GetLastKnownLocationUseCase @Inject constructor(private val repository: TripRepository) {
    suspend operator fun invoke(tripId: String) = repository.lastKnownLocation(tripId)
}

class CountPendingRecordsUseCase @Inject constructor(private val repository: TripRepository) {
    suspend operator fun invoke(tripId: String) = repository.pendingRecordsCount(tripId)
}
