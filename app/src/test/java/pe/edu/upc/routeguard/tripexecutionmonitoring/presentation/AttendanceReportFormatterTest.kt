package pe.edu.upc.routeguard.tripexecutionmonitoring.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.BoardingState
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LiveChild
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LiveTrip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Trip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripStatus
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Waypoint
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.RouteId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.StudentId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.TripId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.WaypointId

class AttendanceReportFormatterTest {

    @Test
    fun `fromTrip computes counts and attendance percentage accurately`() {
        val trip = Trip(
            id = TripId("trp_1"),
            routeId = RouteId("rt_1"),
            routeName = "Ruta 01 - San Borja",
            status = TripStatus.IN_PROGRESS,
            stops = emptyList(),
            waypoints = listOf(
                Waypoint(WaypointId("w1"), StudentId("s1"), "Mateo Quispe", BoardingState.BOARDED),
                Waypoint(WaypointId("w2"), StudentId("s2"), "Lucía Fernandez", BoardingState.BOARDED),
                Waypoint(WaypointId("w3"), StudentId("s3"), "Joaquín Ramos", BoardingState.MISSING),
                Waypoint(WaypointId("w4"), StudentId("s4"), "Camila Vargas", BoardingState.OMITTED)
            ),
            startedAt = 1_000L,
            completedAt = null
        )

        val summary = AttendanceReportFormatter.fromTrip(trip)

        assertEquals(4, summary.total)
        assertEquals(2, summary.boarded)
        assertEquals(1, summary.absent)
        assertEquals(1, summary.pending)
        assertEquals(50, summary.attendancePercent)
        assertEquals(4, summary.rows.size)
    }

    @Test
    fun `fromLiveTrip falls back to totalChildren when children list is empty`() {
        val liveTrip = LiveTrip(
            tripId = TripId("trp_live"),
            routeId = RouteId("rt_2"),
            routeName = "Ruta 02",
            driverName = "Carlos",
            startedAt = 1_000L,
            boardedCount = 3,
            totalChildren = 4,
            location = null,
            stops = emptyList(),
            children = emptyList()
        )

        val summary = AttendanceReportFormatter.fromLiveTrip(liveTrip)

        assertEquals(4, summary.total)
        assertEquals(3, summary.boarded)
        assertEquals(0, summary.absent)
        assertEquals(1, summary.pending)
        assertEquals(75, summary.attendancePercent)
    }

    @Test
    fun `fromLiveTrip uses explicit children when available`() {
        val liveTrip = LiveTrip(
            tripId = TripId("trp_live"),
            routeId = RouteId("rt_2"),
            routeName = "Ruta 02",
            driverName = "Carlos",
            startedAt = 1_000L,
            boardedCount = 1,
            totalChildren = 2,
            location = null,
            stops = emptyList(),
            children = listOf(
                LiveChild(StudentId("s1"), "Ana", BoardingState.BOARDED),
                LiveChild(StudentId("s2"), "Luis", BoardingState.OMITTED)
            )
        )

        val summary = AttendanceReportFormatter.fromLiveTrip(liveTrip)

        assertEquals(2, summary.total)
        assertEquals(1, summary.boarded)
        assertEquals(1, summary.absent)
        assertEquals(0, summary.pending)
    }

    @Test
    fun `computeSummary handles empty roster without division by zero`() {
        val summary = AttendanceReportFormatter.computeSummary(emptyList())
        assertEquals(0, summary.total)
        assertEquals(0, summary.attendancePercent)
    }

    @Test
    fun `toCsv formats summary header and student rows`() {
        val summary = AttendanceReportFormatter.computeSummary(
            listOf(
                AttendanceRow("Mateo Quispe", BoardingState.BOARDED),
                AttendanceRow("Camila Vargas", BoardingState.OMITTED)
            )
        )

        val csv = AttendanceReportFormatter.toCsv("Ruta 01", summary)

        assertTrue(csv.contains("Route,Total,Boarded,Absent,Pending,AttendancePercent"))
        assertTrue(csv.contains("\"Ruta 01\",2,1,1,0,50%"))
        assertTrue(csv.contains("\"Mateo Quispe\",BOARDED"))
        assertTrue(csv.contains("\"Camila Vargas\",OMITTED"))
    }
}
