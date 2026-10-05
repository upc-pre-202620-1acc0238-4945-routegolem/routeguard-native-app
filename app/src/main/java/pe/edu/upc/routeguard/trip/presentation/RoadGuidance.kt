package pe.edu.upc.routeguard.trip.presentation

import pe.edu.upc.routeguard.trip.application.EstimateArrivalUseCase
import pe.edu.upc.routeguard.trip.application.GetRoadRouteUseCase
import pe.edu.upc.routeguard.trip.domain.ArrivalEstimate
import pe.edu.upc.routeguard.trip.domain.NextStop
import pe.edu.upc.routeguard.shared.domain.Coordinates
import javax.inject.Inject

/**
 * Keeps the road route of a trip and the arrival estimate to its next stop, asking Mapbox only
 * when something changed (new stops, new next stop) or after [REFRESH_MS]. One instance per
 * ViewModel.
 */
class RoadGuidance @Inject constructor(
    private val getRoadRoute: GetRoadRouteUseCase,
    private val estimateArrival: EstimateArrivalUseCase
) {
    private var pathKey: String? = null
    private var path: List<Coordinates>? = null
    private var pathFailedAt = 0L

    private var estimateStopOrder: Int? = null
    private var lastEstimateAt = 0L
    private var lastEstimate: ArrivalEstimate? = null

    /** Road path through [points]; null (so the map keeps straight lines) when Mapbox is unavailable. */
    suspend fun pathFor(points: List<Coordinates>): List<Coordinates>? {
        if (points.size < 2) return null
        val key = points.joinToString(";") { "${it.latitude},${it.longitude}" }
        if (key == pathKey) return path
        if (System.currentTimeMillis() - pathFailedAt < RETRY_AFTER_FAILURE_MS) return null
        val route = getRoadRoute(points).getOrNull()
        if (route == null) {
            pathFailedAt = System.currentTimeMillis()
            return null
        }
        pathKey = key
        path = route.path
        return path
    }

    /** Estimate from [from] to [stop], reused for [REFRESH_MS] while the next stop does not change. */
    suspend fun arrivalFor(from: Coordinates, stop: NextStop): ArrivalEstimate? {
        val now = System.currentTimeMillis()
        val sameStop = estimateStopOrder == stop.order
        if (sameStop && now - lastEstimateAt < REFRESH_MS) return lastEstimate

        lastEstimateAt = now
        estimateStopOrder = stop.order
        val fresh = estimateArrival(from, stop.coordinates).getOrNull()?.copy(stopOrder = stop.order)
        lastEstimate = fresh ?: lastEstimate.takeIf { sameStop }
        return lastEstimate
    }

    private companion object {
        const val REFRESH_MS = 20_000L
        const val RETRY_AFTER_FAILURE_MS = 30_000L
    }
}
