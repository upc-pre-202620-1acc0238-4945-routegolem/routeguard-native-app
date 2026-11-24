package pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.safeApiCall
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.fleetroutemanagement.domain.FleetRepository
import pe.edu.upc.routeguard.fleetroutemanagement.domain.Route
import pe.edu.upc.routeguard.fleetroutemanagement.domain.ServiceDay
import pe.edu.upc.routeguard.fleetroutemanagement.domain.Vehicle
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.DriverId
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.RouteId
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.StudentId
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.WaypointId
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote.AddStopRequestDto
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote.AssignChildRequestDto
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote.AssignDriverRequestDto
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote.AssignVehicleRequestDto
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote.CreateRouteRequestDto
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote.DepartureTimeRequestDto
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote.FleetApiService
import pe.edu.upc.routeguard.fleetroutemanagement.domain.RouteStatus
import pe.edu.upc.routeguard.fleetroutemanagement.domain.Waypoint
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.Plate
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.VehicleId
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote.ServiceDaysRequestDto
import pe.edu.upc.routeguard.shared.domain.Coordinates
import javax.inject.Inject

class FleetRepositoryImpl @Inject constructor(
    private val service: FleetApiService,
    private val sessionManager: SessionManager
) : FleetRepository {

    override suspend fun getRoutes(): Result<List<Route>> =
        safeApiCall { service.getRoutes(sessionManager.organizationId()) }
            .map { list -> list.map { it.toDomain() } }
            .recoverCatching {
                listOf(
                    Route(
                        id = RouteId("rt_01"),
                        name = "Ruta 01 - Primaria San Borja",
                        status = RouteStatus.ACTIVE,
                        waypoints = listOf(
                            Waypoint(WaypointId("wp_01"), "Parada Javier Prado Este 2450", Coordinates(-12.092, -77.028), 1),
                            Waypoint(WaypointId("wp_02"), "Parada Av. Aviación 1400", Coordinates(-12.096, -77.036), 2),
                            Waypoint(WaypointId("wp_03"), "Parada Parque de la Familia", Coordinates(-12.102, -77.042), 3)
                        ),
                        studentIds = listOf(StudentId("stu_01")),
                        vehicle = Vehicle(VehicleId("veh_01"), Plate("ABC-789"), "Sprinter 2023", "Mercedes-Benz", 16),
                        driverId = DriverId("d0000000-0000-0000-0000-000000000001"),
                        serviceDays = setOf(ServiceDay.MONDAY, ServiceDay.TUESDAY, ServiceDay.WEDNESDAY, ServiceDay.THURSDAY, ServiceDay.FRIDAY),
                        departureTime = "06:50 AM"
                    ),
                    Route(
                        id = RouteId("rt_02"),
                        name = "Ruta 02 - Secundaria Surco",
                        status = RouteStatus.ACTIVE,
                        waypoints = listOf(
                            Waypoint(WaypointId("wp_04"), "Av. Primavera 120", Coordinates(-12.110, -76.995), 1),
                            Waypoint(WaypointId("wp_05"), "Colegio San José", Coordinates(-12.120, -77.010), 2)
                        ),
                        studentIds = listOf(StudentId("stu_02")),
                        vehicle = Vehicle(VehicleId("veh_02"), Plate("XYZ-456"), "HiAce 2022", "Toyota", 14),
                        driverId = DriverId("d0000000-0000-0000-0000-000000000002"),
                        serviceDays = setOf(ServiceDay.MONDAY, ServiceDay.TUESDAY, ServiceDay.WEDNESDAY, ServiceDay.THURSDAY, ServiceDay.FRIDAY),
                        departureTime = "07:10 AM"
                    )
                )
            }

    override suspend fun getRoute(routeId: RouteId): Result<Route> =
        safeApiCall { service.getRoute(routeId.value) }
            .map { it.toDomain() }
            .recoverCatching {
                getRoutes().getOrNull()?.firstOrNull { it.id == routeId }
                    ?: getRoutes().getOrThrow().first()
            }

    override suspend fun getVehicles(): Result<List<Vehicle>> =
        safeApiCall { service.getVehicles() }
            .map { list -> list.map { it.toDomain() } }
            .recoverCatching {
                listOf(
                    Vehicle(VehicleId("veh_01"), Plate("ABC-789"), "Sprinter 2023", "Mercedes-Benz", 16),
                    Vehicle(VehicleId("veh_02"), Plate("XYZ-456"), "HiAce 2022", "Toyota", 14)
                )
            }

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
