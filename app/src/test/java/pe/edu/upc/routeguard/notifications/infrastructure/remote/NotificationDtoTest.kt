package pe.edu.upc.routeguard.notifications.infrastructure.remote

import com.google.gson.Gson
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class NotificationDtoTest {
    @Test
    fun testDeserialization() {
        val json = """{"notificationId": "noti-123", "tripId": "trip-456"}"""
        val dto = Gson().fromJson(json, NotificationDto::class.java)
        assertNotNull(dto)
        assertEquals("noti-123", dto.id.value)
        assertEquals("trip-456", dto.tripId?.value)
    }
}
