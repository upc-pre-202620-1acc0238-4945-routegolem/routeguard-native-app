package pe.edu.upc.routeguard.features.fleet.domain

import pe.edu.upc.routeguard.shared.domain.AggregateRoot
import pe.edu.upc.routeguard.shared.domain.BaseEntity
import pe.edu.upc.routeguard.shared.domain.Coordinates

enum class ServiceDay(val label: String) {
    MONDAY("Lun"),
    TUESDAY("Mar"),
    WEDNESDAY("Mié"),
    THURSDAY("Jue"),
    FRIDAY("Vie"),
    SATURDAY("Sáb"),
    SUNDAY("Dom")
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
    override val id: String,
    val name: String,
    val coordinates: Coordinates,
    val orderIndex: Int
) : BaseEntity<String>

data class Vehicle(
    override val id: String,
    val plate: String,
    val model: String,
    val brand: String,
    val capacity: Int
) : BaseEntity<String>

/** Predefined sequence of stops between an origin and a school. */
data class Route(
    override val id: String,
    val name: String,
    val status: RouteStatus,
    val waypoints: List<Waypoint>,
    val studentIds: List<String>,
    val vehicle: Vehicle?,
    val driverId: String?,
    val serviceDays: Set<ServiceDay>,
    val departureTime: String?
) : AggregateRoot<String> {

    /** The stop count is recalculated whenever a waypoint is added or removed. */
    val stopCount: Int get() = waypoints.size

    val isActive: Boolean get() = status == RouteStatus.ACTIVE

    /** A route can only be modified while it is a draft. */
    val isEditable: Boolean get() = status == RouteStatus.DRAFT

    val hasVehicleAndDriver: Boolean get() = vehicle != null && !driverId.isNullOrBlank()

    /** Everything the backend requires before a route can be activated. */
    val missingForActivation: List<String>
        get() = buildList {
            if (waypoints.isEmpty()) add("al menos una parada")
            if (vehicle == null) add("un vehículo")
            if (driverId.isNullOrBlank()) add("un conductor")
            if (serviceDays.isEmpty()) add("días de servicio")
            if (departureTime.isNullOrBlank()) add("la hora de salida")
        }
}
