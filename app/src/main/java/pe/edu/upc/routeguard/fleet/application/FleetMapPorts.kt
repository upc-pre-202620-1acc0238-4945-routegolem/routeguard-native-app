package pe.edu.upc.routeguard.fleet.application

import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.shared.domain.Place
import pe.edu.upc.routeguard.shared.domain.RoadRoute

/** Port to the geocoding service: find an address, or name what is at a point of the map. */
interface AddressSearch {
    suspend fun search(query: String, near: Coordinates?): Result<List<Place>>

    suspend fun reverse(point: Coordinates): Result<Place?>
}

/** Port to the route planning service of the maps provider. */
interface RoutePlanner {
    /** Path that follows the roads through [points], in order. */
    suspend fun roadRoute(points: List<Coordinates>): Result<RoadRoute>

    /** Indexes of [points] in the best visiting order, keeping the first and the last fixed. */
    suspend fun optimalOrder(points: List<Coordinates>): Result<List<Int>>
}
