package pe.edu.upc.routeguard.trip.domain

import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.trip.domain.valueobject.RouteId
import pe.edu.upc.routeguard.trip.domain.valueobject.StudentId
import pe.edu.upc.routeguard.trip.domain.valueobject.TripId

interface TripRepository {
    suspend fun getAssignedRoutes(): Result<List<AssignedRoute>>

    suspend fun getActiveTrip(): Trip?

    /** Creates the trip for the route and starts it (boarding is open from the start). */
    suspend fun startTrip(routeId: RouteId): Result<Trip>

    /** Saved locally first (device timestamp) and delivered when there is signal. */
    suspend fun setBoardingStatus(tripId: TripId, studentId: StudentId, state: BoardingState): Result<Trip>

    /** Saved locally first and delivered when there is signal. */
    suspend fun recordLocation(record: LocationRecord)

    suspend fun reportIncident(tripId: TripId, incident: Incident): Result<Unit>

    suspend fun completeTrip(tripId: TripId): Result<Trip>

    suspend fun syncOfflineRecords(tripId: TripId): Result<OfflineSyncCompleted>

    suspend fun pendingRecordsCount(tripId: TripId): Int

    /** Administrator: every trip in progress with the position of its vehicle. */
    suspend fun getLiveTrips(): Result<List<LiveTrip>>

    /** Parent: the trip in progress that carries their children, or null when there is none. */
    suspend fun getLiveTripForParent(): Result<LiveTrip?>

    /** Last point recorded by the background GPS for the trip, or null if there is none yet. */
    suspend fun lastKnownLocation(tripId: TripId): Coordinates?
}
