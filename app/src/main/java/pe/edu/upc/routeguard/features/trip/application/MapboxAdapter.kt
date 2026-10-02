package pe.edu.upc.routeguard.features.trip.application

import pe.edu.upc.routeguard.features.trip.domain.ArrivalEstimate
import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.shared.domain.RoadRoute

/** Port to the maps provider (Mapbox) of Trip Execution & Monitoring. */
interface MapboxAdapter {
    /** Route that follows the roads through [points], in order. */
    suspend fun roadRoute(points: List<Coordinates>): Result<RoadRoute>

    /** Distance and time by road between two points. */
    suspend fun estimateArrival(from: Coordinates, to: Coordinates): Result<ArrivalEstimate>
}
