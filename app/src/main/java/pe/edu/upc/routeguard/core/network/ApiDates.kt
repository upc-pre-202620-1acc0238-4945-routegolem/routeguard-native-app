package pe.edu.upc.routeguard.core.network

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** The backend sends ISO-8601 UTC timestamps; java.time is not available below API 26. */
object ApiDates {

    private fun formatter(pattern: String) =
        SimpleDateFormat(pattern, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

    /** Epoch millis of an ISO timestamp such as 2026-10-01T00:42:54.9615672+00:00, or null. */
    fun parse(value: String?): Long? {
        if (value.isNullOrBlank() || value.length < 19) return null
        return try {
            formatter("yyyy-MM-dd'T'HH:mm:ss").parse(value.substring(0, 19))?.time
        } catch (_: Exception) {
            null
        }
    }

    fun format(millis: Long): String = formatter("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Date(millis))
}
