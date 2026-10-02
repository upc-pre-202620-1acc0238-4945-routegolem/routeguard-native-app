package pe.edu.upc.routeguard.features.fleet.domain

import pe.edu.upc.routeguard.shared.domain.Coordinates

interface FleetRepository {
    suspend fun getRoutes(): Result<List<Route>>

    suspend fun getRoute(routeId: String): Result<Route>

    suspend fun getVehicles(): Result<List<Vehicle>>

    suspend fun defineRoute(name: String): Result<Route>

    suspend fun pickWaypoint(routeId: String, name: String, coordinates: Coordinates): Result<Route>

    suspend fun removeWaypoint(routeId: String, waypointId: String): Result<Route>

    suspend fun assignDriver(routeId: String, driverId: String): Result<Route>

    suspend fun assignStudent(routeId: String, studentId: String): Result<Route>

    suspend fun selectVehicle(routeId: String, vehicle: Vehicle): Result<Route>

    suspend fun defineServiceDays(routeId: String, days: Set<ServiceDay>): Result<Route>

    suspend fun setDepartureTime(routeId: String, time: String): Result<Route>

    suspend fun activateRoute(routeId: String): Result<Route>
}
