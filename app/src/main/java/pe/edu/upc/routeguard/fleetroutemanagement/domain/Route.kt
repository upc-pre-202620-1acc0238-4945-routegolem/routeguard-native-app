package pe.edu.upc.routeguard.fleetroutemanagement.domain

import androidx.annotation.StringRes
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.DriverId
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.Plate
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.RouteId
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.StudentId
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.VehicleId
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.WaypointId
import pe.edu.upc.routeguard.shared.domain.AggregateRoot
import pe.edu.upc.routeguard.shared.domain.BaseEntity
import pe.edu.upc.routeguard.shared.domain.Coordinates

enum class ServiceDay(@StringRes val shortRes: Int, val label: String) {
    MONDAY(R.string.day_mon, "Lun"),
    TUESDAY(R.string.day_tue, "Mar"),
    WEDNESDAY(R.string.day_wed, "Mié"),
    THURSDAY(R.string.day_thu, "Jue"),
    FRIDAY(R.string.day_fri, "Vie"),
    SATURDAY(R.string.day_sat, "Sáb"),
    SUNDAY(R.string.day_sun, "Dom")
}

enum class RouteStatus {
    DRAFT, ACTIVE, INACTIVE;

    companion object {
        fun from(value: String): RouteStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DRAFT
    }
}

/** Location where students board or leave the vehicle. */
data class Waypoint(
    override val id: WaypointId,
    val name: String,
    val coordinates: Coordinates,
    val orderIndex: Int
) : BaseEntity<WaypointId>

data class Vehicle(
    override val id: VehicleId,
    val plate: Plate,
    val model: String,
    val brand: String,
    val capacity: Int
) : BaseEntity<VehicleId>

/** Predefined sequence of stops between an origin and a school. */
data class Route(
    override val id: RouteId,
    val name: String,
    val status: RouteStatus,
    val waypoints: List<Waypoint>,
    val studentIds: List<StudentId>,
    val vehicle: Vehicle?,
    val driverId: DriverId?,
    val serviceDays: Set<ServiceDay>,
    val departureTime: String?
) : AggregateRoot<RouteId> {

    /** The stop count is recalculated whenever a waypoint is added or removed. */
    val stopCount: Int get() = waypoints.size

    val isActive: Boolean get() = status == RouteStatus.ACTIVE

    /** A route can only be modified while it is a draft. */
    val isEditable: Boolean get() = status == RouteStatus.DRAFT

    val hasVehicleAndDriver: Boolean get() = vehicle != null && driverId != null && driverId.value.isNotBlank()

    /** Everything the backend requires before a route can be activated. */
    val missingForActivation: List<String>
        get() = buildList {
            if (waypoints.isEmpty()) add("al menos una parada")
            if (vehicle == null) add("un vehículo")
            if (driverId == null || driverId.value.isBlank()) add("un conductor")
            if (serviceDays.isEmpty()) add("días de servicio")
            if (departureTime.isNullOrBlank()) add("la hora de salida")
        }

    /** Resource IDs of missing prerequisites before activation, for localized UI display. */
    val missingActivationRes: List<Int>
        get() = buildList {
            if (waypoints.isEmpty()) add(R.string.missing_waypoint)
            if (vehicle == null) add(R.string.missing_vehicle)
            if (driverId == null || driverId.value.isBlank()) add(R.string.missing_driver)
            if (serviceDays.isEmpty()) add(R.string.missing_service_days)
            if (departureTime.isNullOrBlank()) add(R.string.missing_departure_time)
        }
}
