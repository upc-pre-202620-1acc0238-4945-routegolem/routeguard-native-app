package pe.edu.upc.routeguard.core.network.mapbox

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Mapbox REST APIs (https://api.mapbox.com/). Not the RouteGuard backend: it never receives the JWT. */
interface MapboxApiService {

    /** [coordinates] is "lng,lat;lng,lat;..." (up to 25 points). */
    @GET("directions/v5/mapbox/driving/{coordinates}")
    suspend fun directions(
        @Path("coordinates", encoded = true) coordinates: String,
        @Query("geometries") geometries: String = "geojson",
        @Query("overview") overview: String = "full",
        @Query("access_token") accessToken: String
    ): Response<DirectionsResponseDto>

    /** Best visiting order keeping the first and the last point fixed (up to 12 points). */
    @GET("optimized-trips/v1/mapbox/driving/{coordinates}")
    suspend fun optimize(
        @Path("coordinates", encoded = true) coordinates: String,
        @Query("roundtrip") roundtrip: Boolean = false,
        @Query("source") source: String = "first",
        @Query("destination") destination: String = "last",
        @Query("access_token") accessToken: String
    ): Response<OptimizationResponseDto>

    @GET("search/geocode/v6/forward")
    suspend fun geocode(
        @Query("q") query: String,
        @Query("country") country: String = "pe",
        @Query("limit") limit: Int = 5,
        @Query("proximity") proximity: String? = null,
        @Query("language") language: String = "es",
        @Query("access_token") accessToken: String
    ): Response<GeocodingResponseDto>

    @GET("search/geocode/v6/reverse")
    suspend fun reverseGeocode(
        @Query("longitude") longitude: Double,
        @Query("latitude") latitude: Double,
        @Query("limit") limit: Int = 1,
        @Query("language") language: String = "es",
        @Query("access_token") accessToken: String
    ): Response<GeocodingResponseDto>
}
