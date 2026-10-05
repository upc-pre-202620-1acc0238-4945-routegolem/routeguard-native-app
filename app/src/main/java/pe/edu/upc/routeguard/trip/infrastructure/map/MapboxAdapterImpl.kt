package pe.edu.upc.routeguard.trip.infrastructure.map

import pe.edu.upc.routeguard.core.network.mapbox.MapboxClient
import pe.edu.upc.routeguard.trip.application.MapboxAdapter
import pe.edu.upc.routeguard.trip.domain.ArrivalEstimate
import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.shared.domain.RoadRoute
import javax.inject.Inject
import kotlin.math.ceil

class MapboxAdapterImpl @Inject constructor(
    private val client: MapboxClient
) : MapboxAdapter {

    override suspend fun roadRoute(points: List<Coordinates>): Result<RoadRoute> = client.roadRoute(points)

    override suspend fun estimateArrival(from: Coordinates, to: Coordinates): Result<ArrivalEstimate> =
        client.roadRoute(listOf(from, to)).map { route ->
            ArrivalEstimate(
                stopOrder = null,
                distanceMeters = route.distanceMeters,
                etaMinutes = ceil(route.durationSeconds / 60.0).toInt().coerceAtLeast(1)
            )
        }
}
