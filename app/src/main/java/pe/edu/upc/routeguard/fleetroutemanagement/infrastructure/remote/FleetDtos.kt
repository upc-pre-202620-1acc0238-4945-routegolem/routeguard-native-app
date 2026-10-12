package pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote

data class StopDto(
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["stopId", "stop_id"])
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val order: Int
)

data class RouteVehicleDto(
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["routeVehicleId", "routeVehicle_id", "vehicleId", "vehicle_id"])
    val id: String,
    val plate: String,
    val model: String?,
    val brand: String?,
    val capacity: Int
)

data class AssignmentDto(
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["assignmentId", "assignment_id"])
    val id: String,
    val driverId: String,
    val childIds: List<String>?
)

data class RouteDto(
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["routeId", "route_id"])
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
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["catalogVehicleId", "catalogVehicle_id", "vehicleId", "vehicle_id"])
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
