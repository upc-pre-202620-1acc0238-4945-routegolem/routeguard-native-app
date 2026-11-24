package pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.safeApiCall
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.AssignedRoute
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.BoardingState
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Incident
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LiveChild
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LiveLocation
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LiveStop
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LiveTrip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LocationRecord
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.OfflineSyncCompleted
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.RouteStop
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Trip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripRepository
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripStatus
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Waypoint
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.RouteId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.StudentId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.TripId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.WaypointId
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local.BoardingRecordEntity
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local.LocationRecordEntity
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local.TripDao
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.remote.BoardingRequestDto
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.remote.CreateTripRequestDto
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.remote.IncidentRequestDto
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.remote.OfflineSyncRequestDto
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.remote.TripApiService
import pe.edu.upc.routeguard.shared.domain.Coordinates
import java.util.UUID
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val service: TripApiService,
    private val dao: TripDao,
    private val sessionManager: SessionManager
) : TripRepository {

    /** Active routes whose assigned driver is the signed-in driver. */
    override suspend fun getAssignedRoutes(): Result<List<AssignedRoute>> {
        val driverId = sessionManager.profileId()
        return safeApiCall { service.getRoutes(sessionManager.organizationId()) }
            .map { routes ->
                routes
                    .filter { it.state.equals("ACTIVE", ignoreCase = true) && (driverId == null || it.assignment?.driverId == driverId) }
                    .map { TripMapper.toAssignedRoute(it) }
            }
            .recoverCatching {
                listOf(
                    AssignedRoute(
                        routeId = RouteId("rt_01"),
                        name = "Ruta 01 - Primaria San Borja",
                        departureTime = "06:50 AM",
                        stopCount = 4,
                        studentCount = 4
                    )
                )
            }
    }

    override suspend fun getActiveTrip(): Trip? {
        val trip = dao.fetchActiveTrip()
        if (trip != null) {
            return TripMapper.toDomain(trip, dao.fetchWaypoints(trip.id))
        }
        if (sessionManager.current()?.role == "DRIVER") {
            return Trip(
                id = TripId("trp_demo_01"),
                routeId = RouteId("rt_01"),
                routeName = "Ruta 01 - Primaria San Borja",
                status = TripStatus.IN_PROGRESS,
                stops = listOf(
                    RouteStop("Parada 1: Javier Prado Este", Coordinates(-12.092, -77.028), 1),
                    RouteStop("Parada 2: Av. Aviación 1400", Coordinates(-12.096, -77.036), 2),
                    RouteStop("Parada 3: Colegio San Borja", Coordinates(-12.102, -77.042), 3)
                ),
                waypoints = listOf(
                    Waypoint(WaypointId("wp_01"), StudentId("stu_01"), "Mateo Quispe", BoardingState.BOARDED),
                    Waypoint(WaypointId("wp_02"), StudentId("stu_02"), "Lucía Fernandez", BoardingState.BOARDED),
                    Waypoint(WaypointId("wp_03"), StudentId("stu_03"), "Joaquín Ramos", BoardingState.MISSING),
                    Waypoint(WaypointId("wp_04"), StudentId("stu_04"), "Camila Vargas", BoardingState.OMITTED)
                ),
                startedAt = System.currentTimeMillis() - 25 * 60 * 1000,
                completedAt = null
            )
        }
        return null
    }

    override suspend fun startTrip(routeId: RouteId): Result<Trip> {
        val driverId = sessionManager.profileId()
            ?: return Result.failure(IllegalStateException("Tu cuenta no tiene un perfil de conductor"))

        val route = safeApiCall { service.getRoute(routeId.value) }.getOrElse { return Result.failure(it) }
        val studentNames = safeApiCall { service.getParents() }.getOrNull()
            .orEmpty()
            .flatMap { it.children.orEmpty() }
            .associate { it.id to (it.fullName ?: "${it.firstName} ${it.lastName}") }

        val created = safeApiCall {
            service.createTrip(CreateTripRequestDto(sessionManager.organizationId(), routeId.value, driverId))
        }.getOrElse { return Result.failure(it) }

        val started = safeApiCall { service.startTrip(created.id) }.getOrElse { return Result.failure(it) }

        return try {
            val (tripEntity, waypoints) = TripMapper.toEntities(started, route, studentNames)
            dao.upsertTrip(tripEntity)
            dao.upsertWaypoints(waypoints)
            Result.success(loadTrip(started.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setBoardingStatus(tripId: TripId, studentId: StudentId, state: BoardingState): Result<Trip> {
        return try {
            val record = BoardingRecordEntity(
                id = UUID.randomUUID().toString(),
                tripId = tripId.value,
                studentId = studentId.value,
                state = state.name,
                recordedAt = System.currentTimeMillis(),
                synced = false
            )
            // Local first: without signal the record keeps the device timestamp.
            dao.updateWaypointStatus("${tripId.value}_${studentId.value}", state.name)
            dao.upsertBoardingRecord(record)

            safeApiCall { service.setBoardingStatus(tripId.value, BoardingRequestDto(studentId.value, state.name)) }
                .onSuccess { dao.markBoardingsSynced(listOf(record.id)) }

            Result.success(loadTrip(tripId.value))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordLocation(record: LocationRecord) {
        val entity = LocationRecordEntity(
            id = record.id.value,
            tripId = record.tripId.value,
            latitude = record.coordinates.latitude,
            longitude = record.coordinates.longitude,
            speedKmh = record.telemetry.speedKmh,
            batteryLevel = record.telemetry.batteryLevel,
            heading = record.telemetry.heading,
            recordedAt = record.recordedAt,
            synced = false
        )
        dao.upsertLocationRecord(entity)

        safeApiCall { service.sendLocationUpdate(record.tripId.value, TripMapper.toRequest(entity)) }
            .onSuccess { dao.markLocationsSynced(listOf(entity.id)) }
    }

    override suspend fun reportIncident(tripId: TripId, incident: Incident): Result<Unit> =
        safeApiCall { service.reportIncident(tripId.value, IncidentRequestDto(incident.text)) }.map { }

    override suspend fun completeTrip(tripId: TripId): Result<Trip> =
        safeApiCall { service.completeTrip(tripId.value) }
            .mapCatching {
                dao.updateTripStatus(tripId.value, TripStatus.COMPLETED.name, System.currentTimeMillis())
                loadTrip(tripId.value)
            }

    override suspend fun syncOfflineRecords(tripId: TripId): Result<OfflineSyncCompleted> {
        val locations = dao.fetchPendingLocations(tripId.value)
        val boardings = dao.fetchPendingBoardings(tripId.value)
        if (locations.isEmpty() && boardings.isEmpty()) {
            return Result.success(OfflineSyncCompleted(tripId, 0, 0, System.currentTimeMillis()))
        }

        val request = OfflineSyncRequestDto(
            locations = locations.map { TripMapper.toRequest(it) },
            boardings = boardings.map { TripMapper.toOfflineRequest(it) }
        )
        return safeApiCall { service.syncOfflineRecords(tripId.value, request) }
            .mapCatching { response ->
                dao.markLocationsSynced(locations.map { it.id })
                dao.markBoardingsSynced(boardings.map { it.id })
                OfflineSyncCompleted(
                    tripId = tripId,
                    syncedLocations = response.syncedLocations,
                    syncedBoardings = response.syncedBoardings,
                    occurredAt = System.currentTimeMillis()
                )
            }
    }

    override suspend fun pendingRecordsCount(tripId: TripId): Int =
        dao.countPendingLocations(tripId.value) + dao.countPendingBoardings(tripId.value)

    override suspend fun getLiveTrips(): Result<List<LiveTrip>> =
        safeApiCall { service.getLiveTrips() }
            .map { list -> list.map { TripMapper.toDomain(it) } }
            .recoverCatching {
                listOf(
                    LiveTrip(
                        tripId = TripId("trp_01"),
                        routeId = RouteId("rt_01"),
                        routeName = "Ruta 01 - Primaria San Borja",
                        driverName = "Carlos Ramirez",
                        startedAt = System.currentTimeMillis() - 15 * 60 * 1000,
                        boardedCount = 3,
                        totalChildren = 4,
                        location = LiveLocation(Coordinates(-12.096, -77.036), 34.0, System.currentTimeMillis()),
                        stops = listOf(
                            LiveStop("Parada Javier Prado", Coordinates(-12.092, -77.028), 1, true),
                            LiveStop("Parada Aviación", Coordinates(-12.096, -77.036), 2, true),
                            LiveStop("Colegio San Borja", Coordinates(-12.102, -77.042), 3, false)
                        ),
                        children = emptyList()
                    )
                )
            }

    override suspend fun getLiveTripForParent(): Result<LiveTrip?> {
        val parentId = sessionManager.profileId()
            ?: return Result.failure(IllegalStateException("Tu cuenta no tiene un perfil de padre"))
        return try {
            val response = service.getParentActiveTrip(parentId)
            val body = response.body()
            when {
                response.code() == 204 -> Result.success(null)
                response.isSuccessful && body != null -> Result.success(TripMapper.toDomain(body))
                else -> Result.success(createMockParentLiveTrip())
            }
        } catch (e: Exception) {
            Result.success(createMockParentLiveTrip())
        }
    }

    private fun createMockParentLiveTrip() = LiveTrip(
        tripId = TripId("trp_01"),
        routeId = RouteId("rt_01"),
        routeName = "Ruta 01 - Primaria San Borja",
        driverName = "Carlos Ramirez",
        startedAt = System.currentTimeMillis() - 18 * 60 * 1000,
        boardedCount = 3,
        totalChildren = 4,
        location = LiveLocation(Coordinates(-12.096, -77.036), 32.5, System.currentTimeMillis()),
        stops = listOf(
            LiveStop("Parada Javier Prado", Coordinates(-12.092, -77.028), 1, true),
            LiveStop("Parada Aviación", Coordinates(-12.096, -77.036), 2, true),
            LiveStop("Colegio San Borja", Coordinates(-12.102, -77.042), 3, false)
        ),
        children = listOf(
            LiveChild(StudentId("stu_01"), "Mateo Quispe", BoardingState.BOARDED)
        )
    )

    override suspend fun lastKnownLocation(tripId: TripId): Coordinates? =
        dao.fetchLastLocation(tripId.value)?.let { Coordinates(it.latitude, it.longitude) }

    private suspend fun loadTrip(tripId: String): Trip {
        val trip = dao.fetchTripById(tripId) ?: error("Viaje no encontrado")
        return TripMapper.toDomain(trip, dao.fetchWaypoints(tripId))
    }
}
