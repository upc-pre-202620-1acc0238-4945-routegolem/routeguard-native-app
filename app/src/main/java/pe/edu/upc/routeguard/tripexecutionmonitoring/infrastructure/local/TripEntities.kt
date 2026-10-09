package pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "route_id")
    val routeId: String,
    @ColumnInfo(name = "route_name")
    val routeName: String,
    val status: String,
    @ColumnInfo(name = "started_at")
    val startedAt: Long?,
    @ColumnInfo(name = "completed_at")
    val completedAt: Long?,
    /** Route stops serialized as JSON. */
    @ColumnInfo(name = "stops_json")
    val stopsJson: String
)

@Entity(tableName = "trip_waypoints")
data class WaypointEntity(
    /** Trip id + student id. */
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "trip_id")
    val tripId: String,
    @ColumnInfo(name = "student_id")
    val studentId: String,
    @ColumnInfo(name = "student_name")
    val studentName: String,
    val status: String
)

@Entity(tableName = "location_records")
data class LocationRecordEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "trip_id")
    val tripId: String,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "speed_kmh")
    val speedKmh: Double,
    @ColumnInfo(name = "battery_level")
    val batteryLevel: Int,
    val heading: Double,
    @ColumnInfo(name = "recorded_at")
    val recordedAt: Long,
    val synced: Boolean
)

@Entity(tableName = "boarding_records")
data class BoardingRecordEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "trip_id")
    val tripId: String,
    @ColumnInfo(name = "student_id")
    val studentId: String,
    val state: String,
    @ColumnInfo(name = "recorded_at")
    val recordedAt: Long,
    val synced: Boolean
)
