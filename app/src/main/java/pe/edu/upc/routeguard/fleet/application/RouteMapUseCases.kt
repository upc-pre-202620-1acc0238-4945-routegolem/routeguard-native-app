package pe.edu.upc.routeguard.fleet.application

import pe.edu.upc.routeguard.fleet.domain.FleetRepository
import pe.edu.upc.routeguard.fleet.domain.Route
import pe.edu.upc.routeguard.fleet.domain.Waypoint
import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.shared.domain.Place
import pe.edu.upc.routeguard.shared.domain.RoadRoute
import javax.inject.Inject

private const val MAX_PATH_POINTS = 25
private const val MIN_OPTIMIZABLE_STOPS = 3
private const val MAX_OPTIMIZABLE_STOPS = 12

/** Looks an address up so a stop can be placed without typing coordinates. */
class SearchAddressUseCase @Inject constructor(private val addressSearch: AddressSearch) {
    suspend operator fun invoke(query: String, near: Coordinates?): Result<List<Place>> {
        if (query.trim().length < 3) {
            return Result.failure(IllegalArgumentException("Escribe al menos 3 letras de la dirección"))
        }
        return addressSearch.search(query.trim(), near)
    }
}

/** Names the place that was tapped on the map, to suggest the name of the stop. */
class ReverseGeocodeUseCase @Inject constructor(private val addressSearch: AddressSearch) {
    suspend operator fun invoke(point: Coordinates): Result<Place?> = addressSearch.reverse(point)
}

/** Path along the roads through the stops of the route. */
class GetRoutePreviewUseCase @Inject constructor(private val planner: RoutePlanner) {
    suspend operator fun invoke(route: Route): Result<RoadRoute> {
        val points = route.waypoints.sortedBy { it.orderIndex }.map { it.coordinates }
        if (points.size < 2) return Result.failure(IllegalStateException("La ruta necesita al menos 2 paradas"))
        return planner.roadRoute(points.take(MAX_PATH_POINTS))
    }
}

/** Proposes the best order for the stops (US-04), keeping the first and the last where they are. */
class SuggestStopOrderUseCase @Inject constructor(private val planner: RoutePlanner) {
    suspend operator fun invoke(route: Route): Result<List<Waypoint>> {
        if (!route.isEditable) {
            return Result.failure(IllegalStateException("Solo se puede reordenar una ruta en borrador"))
        }
        val stops = route.waypoints.sortedBy { it.orderIndex }
        if (stops.size < MIN_OPTIMIZABLE_STOPS) {
            return Result.failure(IllegalStateException("Se necesitan al menos $MIN_OPTIMIZABLE_STOPS paradas"))
        }
        if (stops.size > MAX_OPTIMIZABLE_STOPS) {
            return Result.failure(IllegalStateException("Se pueden ordenar hasta $MAX_OPTIMIZABLE_STOPS paradas"))
        }
        return planner.optimalOrder(stops.map { it.coordinates }).mapCatching { order ->
            val proposal = order.map { stops[it] }
            if (proposal.map { it.id } == stops.map { it.id }) error("Las paradas ya están en el mejor orden")
            proposal
        }
    }
}

/**
 * Applies a new stop order. The backend only appends stops, so the stops are added again in the
 * new order and the old ones are removed afterwards: if something fails midway nothing is lost.
 */
class ApplyStopOrderUseCase @Inject constructor(private val repository: FleetRepository) {
    suspend operator fun invoke(route: Route, newOrder: List<Waypoint>): Result<Route> {
        if (!route.isEditable) {
            return Result.failure(IllegalStateException("Solo se puede reordenar una ruta en borrador"))
        }
        var current = route
        for (stop in newOrder) {
            current = repository.pickWaypoint(route.id, stop.name, stop.coordinates)
                .getOrElse { return Result.failure(it) }
        }
        for (old in route.waypoints) {
            current = repository.removeWaypoint(route.id, old.id).getOrElse { return Result.failure(it) }
        }
        return Result.success(current)
    }
}
