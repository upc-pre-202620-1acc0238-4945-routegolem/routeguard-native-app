package pe.edu.upc.routeguard.shared.domain

/** Route that follows the roads between several points. */
data class RoadRoute(
    val path: List<Coordinates>,
    val distanceMeters: Double,
    val durationSeconds: Double
)

/** Result of looking an address up (geocoding) or of asking what is at a point (reverse geocoding). */
data class Place(
    val name: String,
    val address: String,
    val coordinates: Coordinates
)
