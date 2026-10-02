package pe.edu.upc.routeguard.features.fleet.infrastructure.repositories

import pe.edu.upc.routeguard.features.fleet.domain.Route
import pe.edu.upc.routeguard.features.fleet.domain.RouteStatus
import pe.edu.upc.routeguard.features.fleet.domain.ServiceDay
import pe.edu.upc.routeguard.features.fleet.domain.Vehicle
import pe.edu.upc.routeguard.features.fleet.domain.Waypoint
import pe.edu.upc.routeguard.features.fleet.infrastructure.remote.CatalogVehicleDto
import pe.edu.upc.routeguard.features.fleet.infrastructure.remote.RouteDto
import pe.edu.upc.routeguard.features.fleet.infrastructure.remote.RouteVehicleDto
import pe.edu.upc.routeguard.features.fleet.infrastructure.remote.StopDto
import pe.edu.upc.routeguard.shared.domain.Coordinates

fun StopDto.toDomain() = Waypoint(
    id = id,
    name = name,
    coordinates = Coordinates(latitude, longitude),
    orderIndex = order
)

fun RouteVehicleDto.toDomain() = Vehicle(
    id = id,
    plate = plate,
    model = model.orEmpty(),
    brand = brand.orEmpty(),
    capacity = capacity
)

/** The catalog only has the full model name, so the brand is its first word. */
fun CatalogVehicleDto.toDomain(): Vehicle {
    val fullModel = model.orEmpty()
    return Vehicle(
        id = id,
        plate = plate,
        model = fullModel,
        brand = fullModel.substringBefore(' ').ifBlank { "N/A" },
        capacity = capacity
    )
}

fun RouteDto.toDomain() = Route(
    id = id,
    name = name,
    status = RouteStatus.from(state),
    waypoints = stops.orEmpty().map { it.toDomain() }.sortedBy { it.orderIndex },
    studentIds = assignment?.childIds.orEmpty(),
    vehicle = vehicle?.toDomain(),
    driverId = assignment?.driverId,
    serviceDays = serviceDays.orEmpty()
        .mapNotNull { name -> ServiceDay.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } }
        .toSet(),
    departureTime = departureTime
)
