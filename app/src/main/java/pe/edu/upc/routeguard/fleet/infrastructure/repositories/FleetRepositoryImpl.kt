package pe.edu.upc.routeguard.fleet.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.safeApiCall
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.fleet.domain.FleetRepository
import pe.edu.upc.routeguard.fleet.domain.Route
import pe.edu.upc.routeguard.fleet.domain.ServiceDay
import pe.edu.upc.routeguard.fleet.domain.Vehicle
import pe.edu.upc.routeguard.fleet.domain.valueobject.DriverId
import pe.edu.upc.routeguard.fleet.domain.valueobject.RouteId
import pe.edu.upc.routeguard.fleet.domain.valueobject.StudentId
import pe.edu.upc.routeguard.fleet.domain.valueobject.WaypointId
import pe.edu.upc.routeguard.fleet.infrastructure.remote.AddStopRequestDto
import pe.edu.upc.routeguard.fleet.infrastructure.remote.AssignChildRequestDto
import pe.edu.upc.routeguard.fleet.infrastructure.remote.AssignDriverRequestDto
import pe.edu.upc.routeguard.fleet.infrastructure.remote.AssignVehicleRequestDto
import pe.edu.upc.routeguard.fleet.infrastructure.remote.CreateRouteRequestDto
import pe.edu.upc.routeguard.fleet.infrastructure.remote.DepartureTimeRequestDto
import pe.edu.upc.routeguard.fleet.infrastructure.remote.FleetApiService
import pe.edu.upc.routeguard.fleet.infrastructure.remote.ServiceDaysRequestDto
import pe.edu.upc.routeguard.shared.domain.Coordinates
import javax.inject.Inject

class FleetRepositoryImpl @Inject constructor(
    private val service: FleetApiService,
    private val sessionManager: SessionManager
) : FleetRepository {

    override suspend fun getRoutes(): Result<List<Route>> =
        safeApiCall { service.getRoutes(sessionManager.organizationId()) }
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getRoute(routeId: RouteId): Result<Route> =
        safeApiCall { service.getRoute(routeId.value) }.map { it.toDomain() }

    override suspend fun getVehicles(): Result<List<Vehicle>> =
        safeApiCall { service.getVehicles() }.map { list -> list.map { it.toDomain() } }

    override suspend fun defineRoute(name: String): Result<Route> =
        safeApiCall { service.defineRoute(CreateRouteRequestDto(sessionManager.organizationId(), name)) }
            .map { it.toDomain() }

    override suspend fun pickWaypoint(routeId: RouteId, name: String, coordinates: Coordinates): Result<Route> =
        safeApiCall {
            service.pickWaypoint(routeId.value, AddStopRequestDto(name, coordinates.latitude, coordinates.longitude))
        }.map { it.toDomain() }

    override suspend fun removeWaypoint(routeId: RouteId, waypointId: WaypointId): Result<Route> =
        safeApiCall { service.removeWaypoint(routeId.value, waypointId.value) }.map { it.toDomain() }

    override suspend fun assignDriver(routeId: RouteId, driverId: DriverId): Result<Route> =
        safeApiCall { service.assignDriver(routeId.value, AssignDriverRequestDto(driverId.value)) }.map { it.toDomain() }

    override suspend fun assignStudent(routeId: RouteId, studentId: StudentId): Result<Route> =
        safeApiCall { service.assignStudent(routeId.value, AssignChildRequestDto(studentId.value)) }.map { it.toDomain() }

    override suspend fun selectVehicle(routeId: RouteId, vehicle: Vehicle): Result<Route> =
        safeApiCall {
            service.selectVehicle(
                routeId.value,
                AssignVehicleRequestDto(vehicle.plate.value, vehicle.model, vehicle.brand, vehicle.capacity)
            )
        }.map { it.toDomain() }

    override suspend fun defineServiceDays(routeId: RouteId, days: Set<ServiceDay>): Result<Route> =
        safeApiCall { service.defineServiceDays(routeId.value, ServiceDaysRequestDto(days.map { it.name })) }
            .map { it.toDomain() }

    override suspend fun setDepartureTime(routeId: RouteId, time: String): Result<Route> =
        safeApiCall { service.setDepartureTime(routeId.value, DepartureTimeRequestDto(time)) }
            .map { it.toDomain() }

    override suspend fun activateRoute(routeId: RouteId): Result<Route> =
        safeApiCall { service.activateRoute(routeId.value) }.map { it.toDomain() }
}
