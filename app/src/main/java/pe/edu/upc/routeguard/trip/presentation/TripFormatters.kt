package pe.edu.upc.routeguard.trip.presentation

import pe.edu.upc.routeguard.trip.domain.BoardingState
import pe.edu.upc.routeguard.trip.domain.NextStop
import java.util.Locale

fun formatDistance(meters: Double): String =
    if (meters < 1000) "${meters.toInt()} m" else String.format(Locale.US, "%.1f km", meters / 1000)

/** "850 m · ~3 min" or a hint while there is no GPS point yet. */
fun NextStop.progressText(): String {
    val distance = distanceMeters ?: return "Esperando señal GPS"
    return "${formatDistance(distance)} · ~${etaMinutes ?: 1} min"
}

/** How long ago the vehicle reported its position (device clocks of the driver and the viewer). */
fun formatAge(recordedAtMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val seconds = ((nowMillis - recordedAtMillis) / 1000).coerceAtLeast(0)
    return when {
        seconds < 60 -> "hace $seconds s"
        seconds < 3600 -> "hace ${seconds / 60} min"
        else -> "hace ${seconds / 3600} h"
    }
}

fun BoardingState.parentLabel(): String = when (this) {
    BoardingState.BOARDED -> "A bordo del vehículo"
    BoardingState.OMITTED -> "No asistió hoy"
    BoardingState.MISSING -> "Aún no aborda"
}
