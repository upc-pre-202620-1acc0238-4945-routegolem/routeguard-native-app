package pe.edu.upc.routeguard.core.network.mapbox

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** The JSON samples have the shape of real responses of the Mapbox APIs. */
class MapboxMapperTest {

    private val gson = Gson()

    @Test
    fun `directions response becomes a road route with latitude first`() {
        val json = """
            {"code":"Ok","routes":[{"distance":20683.697,"duration":3319.553,
             "geometry":{"coordinates":[[-77.042873,-12.046405],[-77.04,-12.06],[-77.0297,-12.1211]],"type":"LineString"},
             "legs":[{"distance":10000.0,"duration":1500.0}]}],"waypoints":[]}
        """.trimIndent()

        val route = MapboxMapper.toRoadRoute(gson.fromJson(json, DirectionsResponseDto::class.java))

        assertNotNull(route)
        assertEquals(3, route!!.path.size)
        assertEquals(-12.046405, route.path.first().latitude, 1e-9)
        assertEquals(-77.042873, route.path.first().longitude, 1e-9)
        assertEquals(20683.697, route.distanceMeters, 1e-6)
        assertEquals(3319.553, route.durationSeconds, 1e-6)
    }

    @Test
    fun `directions without route or with a single point gives no route`() {
        val empty = gson.fromJson("""{"code":"NoRoute","routes":[]}""", DirectionsResponseDto::class.java)
        assertNull(MapboxMapper.toRoadRoute(empty))

        val onePoint = gson.fromJson(
            """{"code":"Ok","routes":[{"distance":1.0,"duration":1.0,"geometry":{"coordinates":[[-77.0,-12.0]]}}]}""",
            DirectionsResponseDto::class.java
        )
        assertNull(MapboxMapper.toRoadRoute(onePoint))
    }

    @Test
    fun `optimization waypoint_index is turned into the visiting order`() {
        // Real answer for 4 stops: input stop 1 is visited third and input stop 2 second.
        val json = """
            {"code":"Ok","waypoints":[
              {"waypoint_index":0,"name":"Avenida Alfonso Ugarte"},
              {"waypoint_index":2,"name":"Virgen Milagrosa"},
              {"waypoint_index":1,"name":""},
              {"waypoint_index":3,"name":"Avenida Coronel Inclan"}],"trips":[{}]}
        """.trimIndent()

        val order = MapboxMapper.toVisitOrder(gson.fromJson(json, OptimizationResponseDto::class.java))

        assertEquals(listOf(0, 2, 1, 3), order)
    }

    @Test
    fun `geocoding features become places with latitude first`() {
        val json = """
            {"type":"FeatureCollection","features":[
              {"type":"Feature","geometry":{"type":"Point","coordinates":[-77.074132,-12.077308]},
               "properties":{"name":"Kennedy","full_address":"Kennedy, Lima, Provincia de Lima, Perú","place_formatted":"Lima, Perú"}},
              {"type":"Feature","geometry":{"type":"Point","coordinates":[-77.01741,-12.081533]},
               "properties":{"name":"Pasaje Parque","place_formatted":"Lima, Perú"}},
              {"type":"Feature","properties":{"name":"Sin geometría"}}]}
        """.trimIndent()

        val places = MapboxMapper.toPlaces(gson.fromJson(json, GeocodingResponseDto::class.java))

        assertEquals(2, places.size)
        assertEquals("Kennedy", places[0].name)
        assertEquals("Kennedy, Lima, Provincia de Lima, Perú", places[0].address)
        assertEquals(-12.077308, places[0].coordinates.latitude, 1e-9)
        assertEquals(-77.074132, places[0].coordinates.longitude, 1e-9)
        // Without full_address the formatted place is used.
        assertEquals("Lima, Perú", places[1].address)
    }
}
