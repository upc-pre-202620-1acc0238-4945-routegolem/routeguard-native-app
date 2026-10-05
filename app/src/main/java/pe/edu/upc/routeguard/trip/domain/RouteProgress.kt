package pe.edu.upc.routeguard.trip.domain

import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.shared.domain.distanceTo

/** The stop the vehicle still has to reach, with the distance and an estimated time to get there. */
data class NextStop(
    val name: String,
    val coordinates: Coordinates,
    val order: Int,
    val distanceMeters: Double?,
    val etaMinutes: Int?
)

/** Distance and time by road to a stop, as answered by the maps provider. */
data class ArrivalEstimate(
    val stopOrder: Int?,
    val distanceMeters: Double,
    val etaMinutes: Int
)

/** Replaces the straight-line figures with the road estimate when it is about this same stop. */
fun NextStop.withEstimate(estimate: ArrivalEstimate?): NextStop =
    if (estimate != null && estimate.stopOrder == order) {
        copy(distanceMeters = estimate.distanceMeters, etaMinutes = estimate.etaMinutes)
    } else {
        this
    }

/** Domain rules to follow the progress of a vehicle along its route. */
object RouteProgress {

    /** A stop counts as visited once the vehicle gets this close to it. */
    const val ARRIVAL_RADIUS_METERS = 120.0

    /** Speed used for the ETA while the vehicle is stopped or the speed is unknown. */
    const val ASSUMED_SPEED_KMH = 25.0

    private const val MIN_USEFUL_SPEED_KMH = 5.0

    /** Stops (by order) already visited, including the ones in [previous]. */
    fun visited(stops: List<RouteStop>, location: Coordinates?, previous: Set<Int>): Set<Int> {
        if (location == null) return previous
        val reached = stops.filter { it.coordinates.distanceTo(location) <= ARRIVAL_RADIUS_METERS }.map { it.order }
        return previous + reached
    }

    /** First stop, in route order, that was not visited yet. */
    fun nextStop(
        stops: List<RouteStop>,
        visited: Set<Int>,
        location: Coordinates?,
        speedKmh: Double?
    ): NextStop? {
        val stop = stops.sortedBy { it.order }.firstOrNull { it.order !in visited } ?: return null
        val distance = location?.distanceTo(stop.coordinates)
        val effectiveSpeed = speedKmh?.takeIf { it >= MIN_USEFUL_SPEED_KMH } ?: ASSUMED_SPEED_KMH
        val eta = distance?.let { Math.ceil(it / (effectiveSpeed * 1000.0 / 60.0)).toInt().coerceAtLeast(1) }
        return NextStop(stop.name, stop.coordinates, stop.order, distance, eta)
    }
}
