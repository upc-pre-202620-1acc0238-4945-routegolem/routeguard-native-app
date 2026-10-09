package pe.edu.upc.routeguard.tripexecutionmonitoring.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.shared.domain.distanceTo

class RouteProgressTest {

    private val school = Coordinates(-12.0464, -77.0428)
    private val kennedy = Coordinates(-12.1211, -77.0297)
    private val sanMiguel = Coordinates(-12.0776, -77.0824)

    private val stops = listOf(
        RouteStop("Colegio San Martin", school, 1),
        RouteStop("Parque Kennedy", kennedy, 2),
        RouteStop("Plaza San Miguel", sanMiguel, 3)
    )

    @Test
    fun `haversine distance between two stops is about 8 km`() {
        val meters = school.distanceTo(kennedy)
        assertTrue("was $meters", meters in 8_000.0..8_500.0)
    }

    @Test
    fun `a stop is visited only when the vehicle is within the arrival radius`() {
        val near = Coordinates(-12.1212, -77.0298) // a few meters from Parque Kennedy
        val far = Coordinates(-12.1190, -77.0297) // ~240 m away

        assertEquals(setOf(2), RouteProgress.visited(stops, near, emptySet()))
        assertEquals(emptySet<Int>(), RouteProgress.visited(stops, far, emptySet()))
        // Previously visited stops are kept.
        assertEquals(setOf(1, 2), RouteProgress.visited(stops, near, setOf(1)))
        assertEquals(setOf(1), RouteProgress.visited(stops, null, setOf(1)))
    }

    @Test
    fun `next stop is the first not visited in route order`() {
        val next = RouteProgress.nextStop(stops, visited = setOf(1), location = kennedy, speedKmh = null)

        assertEquals(2, next?.order)
        assertEquals("Parque Kennedy", next?.name)
    }

    @Test
    fun `eta uses the assumed speed while the vehicle is almost stopped`() {
        // 8.4 km: 20.2 min at 25 km/h and 10.1 min at 50 km/h, rounded up.
        val stopped = RouteProgress.nextStop(stops, emptySet(), kennedy, speedKmh = 1.0)
        val moving = RouteProgress.nextStop(stops, emptySet(), kennedy, speedKmh = 50.0)

        assertEquals(21, stopped?.etaMinutes)
        assertEquals(11, moving?.etaMinutes)
    }

    @Test
    fun `there is no next stop once every stop was visited`() {
        assertNull(RouteProgress.nextStop(stops, setOf(1, 2, 3), kennedy, null))
    }

    @Test
    fun `without gps the next stop has no distance`() {
        val next = RouteProgress.nextStop(stops, emptySet(), location = null, speedKmh = null)

        assertEquals(1, next?.order)
        assertNull(next?.distanceMeters)
        assertNull(next?.etaMinutes)
    }

    @Test
    fun `road estimate replaces the straight line figures only for the same stop`() {
        val next = RouteProgress.nextStop(stops, emptySet(), kennedy, null)!!

        val sameStop = next.withEstimate(ArrivalEstimate(stopOrder = 1, distanceMeters = 9_000.0, etaMinutes = 17))
        assertEquals(9_000.0, sameStop.distanceMeters!!, 1e-9)
        assertEquals(17, sameStop.etaMinutes)

        val otherStop = next.withEstimate(ArrivalEstimate(stopOrder = 2, distanceMeters = 1.0, etaMinutes = 1))
        assertEquals(next, otherStop)
        assertEquals(next, next.withEstimate(null))
    }
}
