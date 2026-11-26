package pe.edu.upc.routeguard.tripexecutionmonitoring.domain

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.routeguard.core.di.NetworkModule
import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationRepository
import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.tripexecutionmonitoring.application.CancelTripUseCase
import pe.edu.upc.routeguard.tripexecutionmonitoring.application.ReportIncidentUseCase
import pe.edu.upc.routeguard.tripexecutionmonitoring.application.SyncOfflineRecordsUseCase
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.RouteId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.StudentId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.TripId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.WaypointId

class TripDomainAndIncidentTest {

    private fun sampleTrip(status: TripStatus = TripStatus.IN_PROGRESS) = Trip(
        id = TripId("trp_test"),
        routeId = RouteId("rt_test"),
        routeName = "Ruta Test",
        status = status,
        stops = emptyList(),
        waypoints = listOf(
            Waypoint(WaypointId("wp_1"), StudentId("stu_1"), "Mateo", BoardingState.MISSING)
        ),
        startedAt = 10_000L,
        completedAt = null
    )

    @Test
    fun `cancelled trip disables active status and boarding check-in`() {
        val cancelled = sampleTrip(TripStatus.CANCELLED)
        assertFalse(cancelled.isActive)
        assertFalse(cancelled.acceptsRecords)
    }

    @Test
    fun `roadblock incident formats text and passes validation`() = runBlocking {
        val repo = FakeTripRepository()
        val useCase = ReportIncidentUseCase(repo)

        val result = useCase(sampleTrip(), IncidentType.ROADBLOCK, "[+30 min] Avenida cerrada por obras")

        assertTrue(result.isSuccess)
        assertTrue(repo.lastIncident?.text?.startsWith("Bloqueo de vía:") == true)
    }

    @Test
    fun `cancel trip use case marks trip cancelled and saves critical alert`() = runBlocking {
        val tripRepo = FakeTripRepository()
        val notifRepo = FakeNotificationRepository()
        val useCase = CancelTripUseCase(
            tripRepository = tripRepo,
            notificationRepository = notifRepo,
            syncOfflineRecords = SyncOfflineRecordsUseCase(tripRepo)
        )

        val result = useCase(sampleTrip(), "Cierre total de vía")

        assertTrue(result.isSuccess)
        assertEquals(TripStatus.CANCELLED, result.getOrNull()?.status)
        assertEquals(1, notifRepo.saved.size)
        assertTrue(notifRepo.saved.first().hasPanicAlert)
    }

    @Test
    fun `network module resolveBaseUrl falls back safely on blank or slash ci values`() {
        val fallback = "https://routeguard-web-services.onrender.com/api/v1/"
        assertEquals(fallback, NetworkModule.resolveBaseUrl(""))
        assertEquals(fallback, NetworkModule.resolveBaseUrl("/"))
        assertEquals(fallback, NetworkModule.resolveBaseUrl("   /  "))
        assertEquals(
            "https://custom.api.example/v1/",
            NetworkModule.resolveBaseUrl("https://custom.api.example/v1")
        )
    }

    private class FakeTripRepository : TripRepository {
        var lastIncident: Incident? = null

        override suspend fun getAssignedRoutes(): Result<List<AssignedRoute>> = Result.success(emptyList())
        override suspend fun getActiveTrip(): Trip? = null
        override suspend fun startTrip(routeId: RouteId): Result<Trip> = Result.failure(UnsupportedOperationException())
        override suspend fun setBoardingStatus(tripId: TripId, studentId: StudentId, state: BoardingState): Result<Trip> =
            Result.failure(UnsupportedOperationException())
        override suspend fun recordLocation(record: LocationRecord) {}
        override suspend fun reportIncident(tripId: TripId, incident: Incident): Result<Unit> {
            lastIncident = incident
            return Result.success(Unit)
        }
        override suspend fun completeTrip(tripId: TripId): Result<Trip> = Result.failure(UnsupportedOperationException())
        override suspend fun syncOfflineRecords(tripId: TripId): Result<OfflineSyncCompleted> =
            Result.success(OfflineSyncCompleted(tripId, 0, 0, 0L))
        override suspend fun pendingRecordsCount(tripId: TripId): Int = 0
        override suspend fun getLiveTrips(): Result<List<LiveTrip>> = Result.success(emptyList())
        override suspend fun getLiveTripForParent(): Result<LiveTrip?> = Result.success(null)
        override suspend fun lastKnownLocation(tripId: TripId): Coordinates? = null
        override suspend fun getTripHistory(): Result<List<TripHistoryItem>> = Result.success(emptyList())
        override suspend fun cancelTrip(tripId: TripId, reason: String): Result<Trip> =
            Result.success(
                Trip(
                    id = tripId,
                    routeId = RouteId("rt_test"),
                    routeName = "Ruta Test",
                    status = TripStatus.CANCELLED,
                    stops = emptyList(),
                    waypoints = emptyList(),
                    startedAt = 10_000L,
                    completedAt = 20_000L
                )
            )
        override suspend fun reportEarlyAbsence(tripId: TripId?, studentId: StudentId, studentName: String, reason: String): Result<Unit> =
            Result.success(Unit)
    }

    private class FakeNotificationRepository : NotificationRepository {
        val saved = mutableListOf<Notification>()
        override suspend fun getNotifications(): List<Notification> = saved
        override suspend fun save(notification: Notification) { saved += notification }
        override suspend fun markDelivered(notificationId: String): Result<Unit> = Result.success(Unit)
        override suspend fun triggerPanicAlert(tripId: String): Result<Int> = Result.success(1)
        override suspend fun postBroadcastMessage(tripId: String, message: String): Result<Int> = Result.success(1)
        override suspend fun registerDeviceToken(userId: String, token: String): Result<Unit> = Result.success(Unit)
    }
}
