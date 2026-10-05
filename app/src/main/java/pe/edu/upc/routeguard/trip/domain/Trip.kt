package pe.edu.upc.routeguard.trip.domain

import pe.edu.upc.routeguard.shared.domain.AggregateRoot
import pe.edu.upc.routeguard.shared.domain.BaseEntity
import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.trip.domain.valueobject.LocationRecordId
import pe.edu.upc.routeguard.trip.domain.valueobject.RouteId
import pe.edu.upc.routeguard.trip.domain.valueobject.StudentId
import pe.edu.upc.routeguard.trip.domain.valueobject.TripId
import pe.edu.upc.routeguard.trip.domain.valueobject.WaypointId

enum class TripStatus {
    PENDING, IN_PROGRESS, COMPLETED;

    companion object {
        fun from(value: String): TripStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
    }
}

/** Boarding state of one student in the trip. */
enum class BoardingState {
    MISSING, BOARDED, OMITTED;

    companion object {
        fun from(value: String): BoardingState =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MISSING
    }
}

/** Stop of the route the trip follows. */
data class RouteStop(
    val name: String,
    val coordinates: Coordinates,
    val order: Int
)

/** One student of the trip: it is where the boarding is recorded. */
data class Waypoint(
    override val id: WaypointId,
    val studentId: StudentId,
    val studentName: String,
    val status: BoardingState
) : BaseEntity<WaypointId>

/** Aggregate root: physical execution of a route on a specific date and time. */
data class Trip(
    override val id: TripId,
    val routeId: RouteId,
    val routeName: String,
    val status: TripStatus,
    val stops: List<RouteStop>,
    val waypoints: List<Waypoint>,
    val startedAt: Long?,
    val completedAt: Long?
) : AggregateRoot<TripId> {

    val isActive: Boolean get() = status == TripStatus.IN_PROGRESS

    /** A trip that is not in progress (pending, completed) does not accept new records. */
    val acceptsRecords: Boolean get() = status == TripStatus.IN_PROGRESS

    val boardedCount: Int get() = waypoints.count { it.status == BoardingState.BOARDED }

    val absentCount: Int get() = waypoints.count { it.status == BoardingState.OMITTED }
}

/** Route card shown to the driver before starting a trip. */
data class AssignedRoute(
    val routeId: RouteId,
    val name: String,
    val departureTime: String?,
    val stopCount: Int,
    val studentCount: Int
)

/** Value object: vehicle and device measurements sent with every location. */
data class Telemetry(
    val speedKmh: Double,
    val batteryLevel: Int,
    val heading: Double
)

/** Point-in-time location transmitted in the background. */
data class LocationRecord(
    override val id: LocationRecordId,
    val tripId: TripId,
    val coordinates: Coordinates,
    val telemetry: Telemetry,
    /** Device timestamp: it is kept when the record is saved without signal. */
    val recordedAt: Long,
    val synced: Boolean = false
) : BaseEntity<LocationRecordId>

enum class IncidentType(val label: String) {
    TRAFFIC("Tráfico"),
    MECHANICAL("Falla mecánica"),
    MEDICAL("Emergencia médica"),
    ACCIDENT("Accidente"),
    OTHER("Otro")
}

data class Incident(
    val type: IncidentType,
    val description: String
) {
    /** The backend keeps a single free-text description (10 to 500 characters). */
    val text: String get() = "${type.label}: $description"
}
