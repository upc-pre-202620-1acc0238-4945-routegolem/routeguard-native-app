package pe.edu.upc.routeguard.trip.application

import pe.edu.upc.routeguard.trip.domain.ArrivalEstimate
import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.shared.domain.RoadRoute
import javax.inject.Inject

/** Path along the roads through the stops of the route (drawn instead of straight lines). */
class GetRoadRouteUseCase @Inject constructor(private val mapbox: MapboxAdapter) {
    suspend operator fun invoke(points: List<Coordinates>): Result<RoadRoute> {
        if (points.size < 2) return Result.failure(IllegalArgumentException("La ruta necesita al menos 2 paradas"))
        return mapbox.roadRoute(points.take(MAX_POINTS))
    }

    private companion object {
        const val MAX_POINTS = 25
    }
}

/** Real distance and time by road from the vehicle to the next stop. */
class EstimateArrivalUseCase @Inject constructor(private val mapbox: MapboxAdapter) {
    suspend operator fun invoke(from: Coordinates, to: Coordinates): Result<ArrivalEstimate> =
        mapbox.estimateArrival(from, to)
}
