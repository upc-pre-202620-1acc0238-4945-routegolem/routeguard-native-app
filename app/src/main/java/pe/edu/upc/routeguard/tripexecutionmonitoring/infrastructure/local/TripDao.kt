package pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
@JvmSuppressWildcards
interface TripDao {

    @JvmSuppressWildcards
    @Upsert
    suspend fun upsertTrip(entity: TripEntity): Long

    @JvmSuppressWildcards
    @Upsert
    suspend fun upsertWaypoints(entities: List<WaypointEntity>): List<Long>

    @Query("SELECT * FROM trips WHERE status = 'IN_PROGRESS' ORDER BY started_at DESC LIMIT 1")
    suspend fun fetchActiveTrip(): TripEntity?

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun fetchTripById(tripId: String): TripEntity?

    @Query("SELECT * FROM trip_waypoints WHERE trip_id = :tripId ORDER BY student_name")
    suspend fun fetchWaypoints(tripId: String): List<WaypointEntity>

    @JvmSuppressWildcards
    @Query("UPDATE trips SET status = :status, completed_at = :completedAt WHERE id = :tripId")
    suspend fun updateTripStatus(tripId: String, status: String, completedAt: Long?): Int

    @JvmSuppressWildcards
    @Query("UPDATE trip_waypoints SET status = :status WHERE id = :waypointId")
    suspend fun updateWaypointStatus(waypointId: String, status: String): Int

    @JvmSuppressWildcards
    @Upsert
    suspend fun upsertLocationRecord(entity: LocationRecordEntity): Long

    @Query("SELECT * FROM location_records WHERE trip_id = :tripId AND synced = 0 ORDER BY recorded_at")
    suspend fun fetchPendingLocations(tripId: String): List<LocationRecordEntity>

    @JvmSuppressWildcards
    @Query("UPDATE location_records SET synced = 1 WHERE id IN (:ids)")
    suspend fun markLocationsSynced(ids: List<String>): Int

    @Query("SELECT * FROM location_records WHERE trip_id = :tripId ORDER BY recorded_at DESC LIMIT 1")
    suspend fun fetchLastLocation(tripId: String): LocationRecordEntity?

    @JvmSuppressWildcards
    @Query("SELECT COUNT(*) FROM location_records WHERE trip_id = :tripId AND synced = 0")
    suspend fun countPendingLocations(tripId: String): Int

    @JvmSuppressWildcards
    @Upsert
    suspend fun upsertBoardingRecord(entity: BoardingRecordEntity): Long

    @Query("SELECT * FROM boarding_records WHERE trip_id = :tripId AND synced = 0 ORDER BY recorded_at")
    suspend fun fetchPendingBoardings(tripId: String): List<BoardingRecordEntity>

    @JvmSuppressWildcards
    @Query("UPDATE boarding_records SET synced = 1 WHERE id IN (:ids)")
    suspend fun markBoardingsSynced(ids: List<String>): Int

    @JvmSuppressWildcards
    @Query("SELECT COUNT(*) FROM boarding_records WHERE trip_id = :tripId AND synced = 0")
    suspend fun countPendingBoardings(tripId: String): Int
}
