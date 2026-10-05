package pe.edu.upc.routeguard.trip.application

import pe.edu.upc.routeguard.trip.domain.TripRepository
import pe.edu.upc.routeguard.trip.domain.valueobject.TripId
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
    suspend operator fun invoke(tripId: TripId) = repository.lastKnownLocation(tripId)
}

class CountPendingRecordsUseCase @Inject constructor(private val repository: TripRepository) {
    suspend operator fun invoke(tripId: TripId) = repository.pendingRecordsCount(tripId)
}
