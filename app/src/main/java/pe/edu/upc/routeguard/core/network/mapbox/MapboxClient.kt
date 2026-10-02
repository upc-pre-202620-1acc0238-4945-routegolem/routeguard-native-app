package pe.edu.upc.routeguard.core.network.mapbox

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.network.safeApiCall
import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.shared.domain.Place
import pe.edu.upc.routeguard.shared.domain.RoadRoute
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Single entry point to the Mapbox REST APIs (directions, optimization, geocoding). Every
 * bounded context that needs them wraps it in its own port.
 */
@Singleton
class MapboxClient @Inject constructor(
    @Named("mapbox") private val api: MapboxApiService,
    @ApplicationContext context: Context
) {
    private val token: String = context.getString(R.string.mapbox_access_token)

    val isConfigured: Boolean get() = token.startsWith("pk.")

    private fun notConfigured() =
        Result.failure<Nothing>(IllegalStateException("Mapbox no está configurado (falta MAPBOX_ACCESS_TOKEN)"))

    private fun List<Coordinates>.asPath(): String = joinToString(";") { "${it.longitude},${it.latitude}" }

    suspend fun roadRoute(points: List<Coordinates>): Result<RoadRoute> {
        if (!isConfigured) return notConfigured()
        if (points.size !in 2..MAX_DIRECTIONS_POINTS) {
            return Result.failure(IllegalArgumentException("Se necesitan entre 2 y $MAX_DIRECTIONS_POINTS puntos"))
        }
        return safeApiCall { api.directions(points.asPath(), accessToken = token) }
            .mapCatching { MapboxMapper.toRoadRoute(it) ?: error("Mapbox no encontró una ruta por calles") }
    }

    /** Visiting order (input indexes) with the first and the last point fixed. */
    suspend fun optimalOrder(points: List<Coordinates>): Result<List<Int>> {
        if (!isConfigured) return notConfigured()
        if (points.size !in 3..MAX_OPTIMIZATION_POINTS) {
            return Result.failure(IllegalArgumentException("Se necesitan entre 3 y $MAX_OPTIMIZATION_POINTS paradas"))
        }
        return safeApiCall { api.optimize(points.asPath(), accessToken = token) }
            .mapCatching { MapboxMapper.toVisitOrder(it) ?: error("Mapbox no pudo ordenar las paradas") }
    }

    suspend fun search(query: String, near: Coordinates?): Result<List<Place>> {
        if (!isConfigured) return notConfigured()
        return safeApiCall {
            api.geocode(
                query = query,
                proximity = near?.let { "${it.longitude},${it.latitude}" },
                accessToken = token
            )
        }.map { MapboxMapper.toPlaces(it) }
    }

    suspend fun reverse(point: Coordinates): Result<Place?> {
        if (!isConfigured) return notConfigured()
        return safeApiCall {
            api.reverseGeocode(longitude = point.longitude, latitude = point.latitude, accessToken = token)
        }.map { MapboxMapper.toPlaces(it).firstOrNull() }
    }

    private companion object {
        const val MAX_DIRECTIONS_POINTS = 25
        const val MAX_OPTIMIZATION_POINTS = 12
    }
}
