package pe.edu.upc.routeguard.features.fleet.infrastructure.remote

data class StopDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val order: Int
)

data class RouteVehicleDto(
    val id: String,
    val plate: String,
    val model: String?,
    val brand: String?,
    val capacity: Int
)

data class AssignmentDto(
    val id: String,
    val driverId: String,
    val childIds: List<String>?
)

data class RouteDto(
    val id: String,
    val name: String,
    val state: String,
    val departureTime: String?,
    val serviceDays: List<String>?,
    val vehicle: RouteVehicleDto?,
    val assignment: AssignmentDto?,
    val stops: List<StopDto>?
)

/** Fleet catalog entry (the backend does not expose the brand here). */
data class CatalogVehicleDto(
    val id: String,
    val plate: String,
    val model: String?,
    val capacity: Int,
    val status: String?
)

data class CreateRouteRequestDto(val organizationId: String, val name: String)

data class AddStopRequestDto(val name: String, val latitude: Double, val longitude: Double)

data class AssignVehicleRequestDto(val plate: String, val model: String, val brand: String, val capacity: Int)

data class AssignDriverRequestDto(val driverId: String)

data class AssignChildRequestDto(val childId: String)

data class ServiceDaysRequestDto(val days: List<String>)

data class DepartureTimeRequestDto(val departureTime: String)
