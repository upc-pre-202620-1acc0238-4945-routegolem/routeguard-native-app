package pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.route

import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.AssignedRoute
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.ArrivalEstimate
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.NextStop
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.RouteProgress
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Trip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.withEstimate
import pe.edu.upc.routeguard.shared.domain.Coordinates

data class TrackingUiState(
    val assignedRoutes: List<AssignedRoute> = emptyList(),
    val trip: Trip? = null,
    val pendingRecords: Int = 0,
    val currentLocation: Coordinates? = null,
    /** Stops (by order) the vehicle already got close to during this trip. */
    val visitedStops: Set<Int> = emptySet(),
    /** Path along the roads through the stops, once Mapbox answered. */
    val roadPath: List<Coordinates>? = null,
    /** Road distance/time to the next stop. */
    val arrival: ArrivalEstimate? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
) {
    /** Next stop to reach with distance and ETA, once the trip has started. */
    val nextStop: NextStop?
        get() = trip?.let { RouteProgress.nextStop(it.stops, visitedStops, currentLocation, null) }
            ?.withEstimate(arrival)
}
