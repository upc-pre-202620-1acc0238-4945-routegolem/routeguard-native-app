package pe.edu.upc.routeguard.fleetroutemanagement.application

import pe.edu.upc.routeguard.fleetroutemanagement.domain.FleetRepository
import pe.edu.upc.routeguard.fleetroutemanagement.domain.Route
import pe.edu.upc.routeguard.fleetroutemanagement.domain.ServiceDay
import pe.edu.upc.routeguard.fleetroutemanagement.domain.Vehicle
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.DriverId
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.RouteId
import pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.StudentId
import pe.edu.upc.routeguard.shared.domain.Coordinates
import javax.inject.Inject

class GetRoutesUseCase @Inject constructor(private val repository: FleetRepository) {
    suspend operator fun invoke() = repository.getRoutes()
}

class GetRouteByIdUseCase @Inject constructor(private val repository: FleetRepository) {
    suspend operator fun invoke(routeId: RouteId) = repository.getRoute(routeId)
}

class GetVehiclesUseCase @Inject constructor(private val repository: FleetRepository) {
    suspend operator fun invoke() = repository.getVehicles()
}

class DefineRouteUseCase @Inject constructor(private val repository: FleetRepository) {
    suspend operator fun invoke(name: String): Result<Route> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("La ruta necesita un nombre"))
        return repository.defineRoute(name.trim())
    }
}

class PickWaypointUseCase @Inject constructor(private val repository: FleetRepository) {
    suspend operator fun invoke(routeId: RouteId, name: String, latitude: String, longitude: String): Result<Route> {
        val lat = latitude.trim().toDoubleOrNull()
        val lng = longitude.trim().toDoubleOrNull()
        if (name.isBlank() || lat == null || lng == null) {
            return Result.failure(IllegalArgumentException("Ingresa el nombre de la parada y coordenadas válidas"))
        }
        if (lat !in -90.0..90.0 || lng !in -180.0..180.0) {
            return Result.failure(IllegalArgumentException("Las coordenadas están fuera de rango"))
        }
        return repository.pickWaypoint(routeId, name.trim(), Coordinates(lat, lng))
    }
}

/** A driver must be assigned before students can be assigned to the route. */
class AssignStudentsToRouteUseCase @Inject constructor(private val repository: FleetRepository) {
    suspend operator fun invoke(route: Route, studentIds: Collection<StudentId>): Result<Route> {
        if (route.driverId == null || route.driverId.value.isBlank()) {
            return Result.failure(IllegalStateException("Asigna un conductor antes de asignar estudiantes"))
        }
        val pending = studentIds.filter { it !in route.studentIds }
        if (pending.isEmpty()) {
            return Result.failure(IllegalArgumentException("Selecciona al menos un estudiante nuevo"))
        }
        var current = route
        for (studentId in pending) {
            current = repository.assignStudent(route.id, studentId).getOrElse { return Result.failure(it) }
        }
        return Result.success(current)
    }
}

/** Select Vehicle: assigns the vehicle and the driver of the route. */
class SelectVehicleUseCase @Inject constructor(private val repository: FleetRepository) {
    suspend operator fun invoke(routeId: RouteId, vehicle: Vehicle?, driverId: DriverId?): Result<Route> {
        if (vehicle == null || driverId == null || driverId.value.isBlank()) {
            return Result.failure(IllegalArgumentException("Selecciona un vehículo y un conductor"))
        }
        repository.selectVehicle(routeId, vehicle).onFailure { return Result.failure(it) }
        return repository.assignDriver(routeId, driverId)
    }
}

class DefineServiceDaysUseCase @Inject constructor(private val repository: FleetRepository) {
    suspend operator fun invoke(routeId: RouteId, days: Set<ServiceDay>): Result<Route> {
        if (days.isEmpty()) return Result.failure(IllegalArgumentException("Selecciona al menos un día"))
        return repository.defineServiceDays(routeId, days)
    }
}

class SetDepartureTimeUseCase @Inject constructor(private val repository: FleetRepository) {
    suspend operator fun invoke(routeId: RouteId, time: String): Result<Route> {
        if (!TIME_REGEX.matches(time.trim())) {
            return Result.failure(IllegalArgumentException("Usa el formato HH:mm (por ejemplo 06:30)"))
        }
        return repository.setDepartureTime(routeId, time.trim())
    }

    private companion object {
        val TIME_REGEX = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")
    }
}

/** Route Activation Finalized: published for Trip Execution & Monitoring. */
class ActivateRouteUseCase @Inject constructor(private val repository: FleetRepository) {
    suspend operator fun invoke(route: Route): Result<Route> {
        val missing = route.missingForActivation
        if (missing.isNotEmpty()) {
            return Result.failure(IllegalStateException("Falta ${missing.joinToString(", ")} para activar la ruta"))
        }
        return repository.activateRoute(route.id)
    }
}
