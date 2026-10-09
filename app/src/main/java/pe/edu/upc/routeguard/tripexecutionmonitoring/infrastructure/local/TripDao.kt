package pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface TripDao {

    @Upsert
    suspend fun upsertTrip(entity: TripEntity)

    @Upsert
    suspend fun upsertWaypoints(entities: List<WaypointEntity>)

    @Query("SELECT * FROM trips WHERE status = 'IN_PROGRESS' ORDER BY started_at DESC LIMIT 1")
    suspend fun fetchActiveTrip(): TripEntity?

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun fetchTripById(tripId: String): TripEntity?

    @Query("SELECT * FROM trip_waypoints WHERE trip_id = :tripId ORDER BY student_name")
    suspend fun fetchWaypoints(tripId: String): List<WaypointEntity>

    @Query("UPDATE trips SET status = :status, completed_at = :completedAt WHERE id = :tripId")
    suspend fun updateTripStatus(tripId: String, status: String, completedAt: Long?)

    @Query("UPDATE trip_waypoints SET status = :status WHERE id = :waypointId")
    suspend fun updateWaypointStatus(waypointId: String, status: String)

    @Upsert
    suspend fun upsertLocationRecord(entity: LocationRecordEntity)

    @Query("SELECT * FROM location_records WHERE trip_id = :tripId AND synced = 0 ORDER BY recorded_at")
    suspend fun fetchPendingLocations(tripId: String): List<LocationRecordEntity>

    @Query("UPDATE location_records SET synced = 1 WHERE id IN (:ids)")
    suspend fun markLocationsSynced(ids: List<String>)

    @Query("SELECT * FROM location_records WHERE trip_id = :tripId ORDER BY recorded_at DESC LIMIT 1")
    suspend fun fetchLastLocation(tripId: String): LocationRecordEntity?

    @Query("SELECT COUNT(*) FROM location_records WHERE trip_id = :tripId AND synced = 0")
    suspend fun countPendingLocations(tripId: String): Int

    @Upsert
    suspend fun upsertBoardingRecord(entity: BoardingRecordEntity)

    @Query("SELECT * FROM boarding_records WHERE trip_id = :tripId AND synced = 0 ORDER BY recorded_at")
    suspend fun fetchPendingBoardings(tripId: String): List<BoardingRecordEntity>

    @Query("UPDATE boarding_records SET synced = 1 WHERE id IN (:ids)")
    suspend fun markBoardingsSynced(ids: List<String>)

    @Query("SELECT COUNT(*) FROM boarding_records WHERE trip_id = :tripId AND synced = 0")
    suspend fun countPendingBoardings(tripId: String): Int
}
