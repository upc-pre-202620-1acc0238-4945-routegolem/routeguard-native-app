package pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.repositories

import pe.edu.upc.routeguard.fleetroutemanagement.domain.Route
import pe.edu.upc.routeguard.fleetroutemanagement.domain.RouteStatus
import pe.edu.upc.routeguard.fleetroutemanagement.domain.ServiceDay
import pe.edu.upc.routeguard.fleetroutemanagement.domain.Vehicle
import pe.edu.upc.routeguard.fleetroutemanagement.domain.Waypoint
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.DriverId
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.Plate
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.RouteId
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.StudentId
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.VehicleId
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.WaypointId
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote.CatalogVehicleDto
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote.RouteDto
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote.RouteVehicleDto
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote.StopDto
import pe.edu.upc.routeguard.shared.domain.Coordinates

fun StopDto.toDomain() = Waypoint(
    id = WaypointId(id),
    name = name,
    coordinates = Coordinates(latitude, longitude),
    orderIndex = order
)

fun RouteVehicleDto.toDomain() = Vehicle(
    id = VehicleId(id),
    plate = Plate(plate),
    model = model.orEmpty(),
    brand = brand.orEmpty(),
    capacity = capacity
)

/** The catalog only has the full model name, so the brand is its first word. */
fun CatalogVehicleDto.toDomain(): Vehicle {
    val fullModel = model.orEmpty()
    return Vehicle(
        id = VehicleId(id),
        plate = Plate(plate),
        model = fullModel,
        brand = fullModel.substringBefore(' ').ifBlank { "N/A" },
        capacity = capacity
    )
}

fun RouteDto.toDomain() = Route(
    id = RouteId(id),
    name = name,
    status = RouteStatus.from(state),
    waypoints = stops.orEmpty().map { it.toDomain() }.sortedBy { it.orderIndex },
    studentIds = assignment?.childIds.orEmpty().map { StudentId(it) },
    vehicle = vehicle?.toDomain(),
    driverId = assignment?.driverId?.let { DriverId(it) },
    serviceDays = serviceDays.orEmpty()
        .mapNotNull { name -> ServiceDay.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } }
        .toSet(),
    departureTime = departureTime
)
