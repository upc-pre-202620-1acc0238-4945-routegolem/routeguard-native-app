package pe.edu.upc.routeguard.features.fleet.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.safeApiCall
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.features.fleet.domain.FleetRepository
import pe.edu.upc.routeguard.features.fleet.domain.Route
import pe.edu.upc.routeguard.features.fleet.domain.ServiceDay
import pe.edu.upc.routeguard.features.fleet.domain.Vehicle
import pe.edu.upc.routeguard.features.fleet.infrastructure.remote.AddStopRequestDto
import pe.edu.upc.routeguard.features.fleet.infrastructure.remote.AssignChildRequestDto
import pe.edu.upc.routeguard.features.fleet.infrastructure.remote.AssignDriverRequestDto
import pe.edu.upc.routeguard.features.fleet.infrastructure.remote.AssignVehicleRequestDto
import pe.edu.upc.routeguard.features.fleet.infrastructure.remote.CreateRouteRequestDto
import pe.edu.upc.routeguard.features.fleet.infrastructure.remote.DepartureTimeRequestDto
import pe.edu.upc.routeguard.features.fleet.infrastructure.remote.FleetApiService
import pe.edu.upc.routeguard.features.fleet.infrastructure.remote.ServiceDaysRequestDto
import pe.edu.upc.routeguard.shared.domain.Coordinates
import javax.inject.Inject

class FleetRepositoryImpl @Inject constructor(
    private val service: FleetApiService,
    private val sessionManager: SessionManager
) : FleetRepository {

    override suspend fun getRoutes(): Result<List<Route>> =
        safeApiCall { service.getRoutes(sessionManager.organizationId()) }
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getRoute(routeId: String): Result<Route> =
        safeApiCall { service.getRoute(routeId) }.map { it.toDomain() }

    override suspend fun getVehicles(): Result<List<Vehicle>> =
        safeApiCall { service.getVehicles() }.map { list -> list.map { it.toDomain() } }

    override suspend fun defineRoute(name: String): Result<Route> =
        safeApiCall { service.defineRoute(CreateRouteRequestDto(sessionManager.organizationId(), name)) }
            .map { it.toDomain() }

    override suspend fun pickWaypoint(routeId: String, name: String, coordinates: Coordinates): Result<Route> =
        safeApiCall {
            service.pickWaypoint(routeId, AddStopRequestDto(name, coordinates.latitude, coordinates.longitude))
        }.map { it.toDomain() }

    override suspend fun removeWaypoint(routeId: String, waypointId: String): Result<Route> =
        safeApiCall { service.removeWaypoint(routeId, waypointId) }.map { it.toDomain() }

    override suspend fun assignDriver(routeId: String, driverId: String): Result<Route> =
        safeApiCall { service.assignDriver(routeId, AssignDriverRequestDto(driverId)) }.map { it.toDomain() }

    override suspend fun assignStudent(routeId: String, studentId: String): Result<Route> =
        safeApiCall { service.assignStudent(routeId, AssignChildRequestDto(studentId)) }.map { it.toDomain() }

    override suspend fun selectVehicle(routeId: String, vehicle: Vehicle): Result<Route> =
        safeApiCall {
            service.selectVehicle(
                routeId,
                AssignVehicleRequestDto(vehicle.plate, vehicle.model, vehicle.brand, vehicle.capacity)
            )
        }.map { it.toDomain() }

    override suspend fun defineServiceDays(routeId: String, days: Set<ServiceDay>): Result<Route> =
        safeApiCall { service.defineServiceDays(routeId, ServiceDaysRequestDto(days.map { it.name })) }
            .map { it.toDomain() }

    override suspend fun setDepartureTime(routeId: String, time: String): Result<Route> =
        safeApiCall { service.setDepartureTime(routeId, DepartureTimeRequestDto(time)) }
            .map { it.toDomain() }

    override suspend fun activateRoute(routeId: String): Result<Route> =
        safeApiCall { service.activateRoute(routeId) }.map { it.toDomain() }
}
