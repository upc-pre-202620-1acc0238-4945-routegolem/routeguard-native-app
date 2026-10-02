package pe.edu.upc.routeguard.features.trip.infrastructure.remote

// ---- Read model of the Fleet / Stakeholder contexts (route cards and student names) ----

data class StopSummaryDto(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val order: Int
)

data class AssignmentSummaryDto(
    val driverId: String,
    val childIds: List<String>?
)

data class RouteSummaryDto(
    val id: String,
    val name: String,
    val state: String,
    val departureTime: String?,
    val assignment: AssignmentSummaryDto?,
    val stops: List<StopSummaryDto>?
)

data class ChildSummaryDto(
    val id: String,
    val fullName: String?,
    val firstName: String,
    val lastName: String
)

data class ParentSummaryDto(
    val id: String,
    val children: List<ChildSummaryDto>?
)

// ---- Trip contract ----

data class CreateTripRequestDto(
    val organizationId: String,
    val routeId: String,
    val driverId: String
)

data class AttendanceDto(
    val childId: String,
    val boardingState: String
)

data class TripDto(
    val id: String,
    val routeId: String,
    val tripState: String,
    val startTime: String?,
    val endTime: String?,
    val attendances: List<AttendanceDto>?
)

data class BoardingRequestDto(
    val childId: String,
    val boardingState: String
)

data class IncidentRequestDto(val description: String)

data class LocationUpdateRequestDto(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double,
    val batteryLevel: Int,
    val heading: Double,
    val recordedAt: Long
)

data class OfflineBoardingDto(
    val childId: String,
    val boardingState: String,
    val recordedAt: Long
)

data class OfflineSyncRequestDto(
    val locations: List<LocationUpdateRequestDto>,
    val boardings: List<OfflineBoardingDto>
)

data class OfflineSyncResponseDto(
    val syncedLocations: Int,
    val syncedBoardings: Int
)

// ---- Live tracking (parents and administrator) ----

data class LiveStopDto(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val order: Int,
    val reached: Boolean
)

data class LiveLocationDto(
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double,
    val recordedAt: Long
)

data class LiveChildDto(
    val childId: String,
    val name: String,
    val boardingState: String
)

data class LiveTripDto(
    val tripId: String,
    val routeId: String,
    val routeName: String,
    val driverName: String?,
    val startedAt: String?,
    val boardedCount: Int,
    val totalChildren: Int,
    val location: LiveLocationDto?,
    val stops: List<LiveStopDto>?,
    val children: List<LiveChildDto>?
)
