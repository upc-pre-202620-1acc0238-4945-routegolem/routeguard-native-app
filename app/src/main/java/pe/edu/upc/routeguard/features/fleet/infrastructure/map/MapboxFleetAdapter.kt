package pe.edu.upc.routeguard.features.fleet.infrastructure.map

import pe.edu.upc.routeguard.core.network.mapbox.MapboxClient
import pe.edu.upc.routeguard.features.fleet.application.AddressSearch
import pe.edu.upc.routeguard.features.fleet.application.RoutePlanner
import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.shared.domain.Place
import pe.edu.upc.routeguard.shared.domain.RoadRoute
import javax.inject.Inject

/** Mapbox implementation of the geocoding and route planning ports of Fleet & Route Management. */
class MapboxFleetAdapter @Inject constructor(
    private val client: MapboxClient
) : AddressSearch, RoutePlanner {

    override suspend fun search(query: String, near: Coordinates?): Result<List<Place>> =
        client.search(query, near)

    override suspend fun reverse(point: Coordinates): Result<Place?> = client.reverse(point)

    override suspend fun roadRoute(points: List<Coordinates>): Result<RoadRoute> = client.roadRoute(points)

    override suspend fun optimalOrder(points: List<Coordinates>): Result<List<Int>> = client.optimalOrder(points)
}
