package pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.repositories

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pe.edu.upc.routeguard.core.network.ApiDates
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.AssignedRoute
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.BoardingState
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LiveChild
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LiveLocation
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LiveStop
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LiveTrip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.RouteStop
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Trip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripStatus
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Waypoint
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.LocationRecordId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.RouteId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.StudentId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.TripId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.WaypointId
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local.BoardingRecordEntity
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local.LocationRecordEntity
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local.TripEntity
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local.WaypointEntity
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.remote.LiveTripDto
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.remote.LocationUpdateRequestDto
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.remote.OfflineBoardingDto
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.remote.RouteSummaryDto
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.remote.StopSummaryDto
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.remote.TripDto
import pe.edu.upc.routeguard.shared.domain.Coordinates

/** Maps between remote DTOs, Room entities and the Trip domain model. */
object TripMapper {

    private val gson = Gson()
    private val stopsType = object : TypeToken<List<StopSummaryDto>>() {}.type

    fun toAssignedRoute(dto: RouteSummaryDto) = AssignedRoute(
        routeId = RouteId(dto.id),
        name = dto.name,
        departureTime = dto.departureTime,
        stopCount = dto.stops.orEmpty().size,
        studentCount = dto.assignment?.childIds.orEmpty().size
    )

    /**
     * Builds the local snapshot of a trip: one waypoint per student assigned to the route, with the
     * attendance recorded so far and the student names resolved from the parents.
     */
    fun toEntities(
        trip: TripDto,
        route: RouteSummaryDto,
        studentNames: Map<String, String>
    ): Pair<TripEntity, List<WaypointEntity>> {
        val attendances = trip.attendances.orEmpty().associate { it.childId to it.boardingState }
        val tripEntity = TripEntity(
            id = trip.id,
            routeId = trip.routeId,
            routeName = route.name,
            status = TripStatus.from(trip.tripState).name,
            startedAt = ApiDates.parse(trip.startTime),
            completedAt = ApiDates.parse(trip.endTime),
            stopsJson = gson.toJson(route.stops.orEmpty())
        )
        val waypoints = route.assignment?.childIds.orEmpty().map { childId ->
            WaypointEntity(
                id = "${trip.id}_$childId",
                tripId = trip.id,
                studentId = childId,
                studentName = studentNames[childId] ?: "Estudiante",
                status = BoardingState.from(attendances[childId].orEmpty()).name
            )
        }
        return tripEntity to waypoints
    }

    fun toDomain(trip: TripEntity, waypoints: List<WaypointEntity>) = Trip(
        id = TripId(trip.id),
        routeId = RouteId(trip.routeId),
        routeName = trip.routeName,
        status = TripStatus.from(trip.status),
        stops = parseStops(trip.stopsJson),
        waypoints = waypoints.map {
            Waypoint(
                id = WaypointId(it.id),
                studentId = StudentId(it.studentId),
                studentName = it.studentName,
                status = BoardingState.from(it.status)
            )
        },
        startedAt = trip.startedAt,
        completedAt = trip.completedAt
    )

    private fun parseStops(json: String): List<RouteStop> {
        val parsed: List<StopSummaryDto> = try {
            gson.fromJson(json, stopsType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
        return parsed.sortedBy { it.order }
            .map { RouteStop(it.name, Coordinates(it.latitude, it.longitude), it.order) }
    }

    fun toDomain(dto: LiveTripDto) = LiveTrip(
        tripId = TripId(dto.tripId),
        routeId = RouteId(dto.routeId),
        routeName = dto.routeName,
        driverName = dto.driverName.orEmpty(),
        startedAt = ApiDates.parse(dto.startedAt),
        boardedCount = dto.boardedCount,
        totalChildren = dto.totalChildren,
        location = dto.location?.let {
            LiveLocation(Coordinates(it.latitude, it.longitude), it.speedKmh, it.recordedAt)
        },
        stops = dto.stops.orEmpty()
            .sortedBy { it.order }
            .map { LiveStop(it.name, Coordinates(it.latitude, it.longitude), it.order, it.reached) },
        children = dto.children.orEmpty().map { LiveChild(StudentId(it.childId), it.name, BoardingState.from(it.boardingState)) }
    )

    fun toRequest(entity: LocationRecordEntity) = LocationUpdateRequestDto(
        id = entity.id,
        latitude = entity.latitude,
        longitude = entity.longitude,
        speedKmh = entity.speedKmh,
        batteryLevel = entity.batteryLevel,
        heading = entity.heading,
        recordedAt = entity.recordedAt
    )

    fun toOfflineRequest(entity: BoardingRecordEntity) = OfflineBoardingDto(
        childId = entity.studentId,
        boardingState = entity.state,
        recordedAt = entity.recordedAt
    )
}
