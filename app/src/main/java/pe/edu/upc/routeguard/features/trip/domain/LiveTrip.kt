package pe.edu.upc.routeguard.features.trip.domain

import pe.edu.upc.routeguard.shared.domain.Coordinates

data class LiveStop(
    val name: String,
    val coordinates: Coordinates,
    val order: Int,
    /** The vehicle already crossed the 500 m geofence of this stop. */
    val reached: Boolean
)

data class LiveLocation(
    val coordinates: Coordinates,
    val speedKmh: Double,
    val recordedAt: Long
)

data class LiveChild(
    val id: String,
    val name: String,
    val state: BoardingState
)

/** Read model of a trip in progress, as watched by the parents and the administrator. */
data class LiveTrip(
    val tripId: String,
    val routeId: String,
    val routeName: String,
    val driverName: String,
    val startedAt: Long?,
    val boardedCount: Int,
    val totalChildren: Int,
    val location: LiveLocation?,
    val stops: List<LiveStop>,
    /** Only the children of the signed-in parent (empty for the administrator). */
    val children: List<LiveChild>
) {
    val nextStop: NextStop?
        get() = RouteProgress.nextStop(
            stops = stops.map { RouteStop(it.name, it.coordinates, it.order) },
            visited = stops.filter { it.reached }.map { it.order }.toSet(),
            location = location?.coordinates,
            speedKmh = location?.speedKmh
        )
}
