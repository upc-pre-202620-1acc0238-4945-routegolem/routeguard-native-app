package pe.edu.upc.routeguard.core.network.mapbox

import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.shared.domain.Place
import pe.edu.upc.routeguard.shared.domain.RoadRoute

/** Maps Mapbox responses (GeoJSON order: longitude first) to the shared domain models. */
object MapboxMapper {

    fun toRoadRoute(dto: DirectionsResponseDto): RoadRoute? {
        val route = dto.routes?.firstOrNull() ?: return null
        val path = route.geometry?.coordinates.orEmpty().mapNotNull { pair ->
            if (pair.size >= 2) Coordinates(latitude = pair[1], longitude = pair[0]) else null
        }
        if (path.size < 2) return null
        return RoadRoute(path, route.distance, route.duration)
    }

    /**
     * Input indexes in the order they should be visited. The API answers, for each input point
     * (in input order), the position it takes in the optimized trip.
     */
    fun toVisitOrder(dto: OptimizationResponseDto): List<Int>? {
        val waypoints = dto.waypoints ?: return null
        if (waypoints.isEmpty()) return null
        return waypoints.indices.sortedBy { waypoints[it].waypointIndex }
    }

    fun toPlaces(dto: GeocodingResponseDto): List<Place> =
        dto.features.orEmpty().mapNotNull { feature ->
            val point = feature.geometry?.coordinates ?: return@mapNotNull null
            if (point.size < 2) return@mapNotNull null
            val properties = feature.properties
            val name = properties?.name.orEmpty()
            Place(
                name = name,
                address = properties?.fullAddress ?: properties?.placeFormatted ?: name,
                coordinates = Coordinates(latitude = point[1], longitude = point[0])
            )
        }
}
