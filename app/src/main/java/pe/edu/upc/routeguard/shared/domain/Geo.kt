package pe.edu.upc.routeguard.shared.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

private fun Double.toRadians(): Double = this * Math.PI / 180.0

/** Haversine distance in meters between two WGS84 points. */
fun Coordinates.distanceTo(other: Coordinates): Double {
    val dLat = (other.latitude - latitude).toRadians()
    val dLng = (other.longitude - longitude).toRadians()
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(latitude.toRadians()) * cos(other.latitude.toRadians()) * sin(dLng / 2) * sin(dLng / 2)
    return 2 * EARTH_RADIUS_METERS * atan2(sqrt(a), sqrt(1 - a))
}
