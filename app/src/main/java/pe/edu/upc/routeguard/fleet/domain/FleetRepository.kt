package pe.edu.upc.routeguard.fleet.domain

import pe.edu.upc.routeguard.fleet.domain.valueobject.DriverId
import pe.edu.upc.routeguard.fleet.domain.valueobject.RouteId
import pe.edu.upc.routeguard.fleet.domain.valueobject.StudentId
import pe.edu.upc.routeguard.fleet.domain.valueobject.WaypointId
import pe.edu.upc.routeguard.shared.domain.Coordinates

interface FleetRepository {
    suspend fun getRoutes(): Result<List<Route>>

    suspend fun getRoute(routeId: RouteId): Result<Route>

    suspend fun getVehicles(): Result<List<Vehicle>>

    suspend fun defineRoute(name: String): Result<Route>

    suspend fun pickWaypoint(routeId: RouteId, name: String, coordinates: Coordinates): Result<Route>

    suspend fun removeWaypoint(routeId: RouteId, waypointId: WaypointId): Result<Route>

    suspend fun assignDriver(routeId: RouteId, driverId: DriverId): Result<Route>

    suspend fun assignStudent(routeId: RouteId, studentId: StudentId): Result<Route>

    suspend fun selectVehicle(routeId: RouteId, vehicle: Vehicle): Result<Route>

    suspend fun defineServiceDays(routeId: RouteId, days: Set<ServiceDay>): Result<Route>

    suspend fun setDepartureTime(routeId: RouteId, time: String): Result<Route>

    suspend fun activateRoute(routeId: RouteId): Result<Route>
}
