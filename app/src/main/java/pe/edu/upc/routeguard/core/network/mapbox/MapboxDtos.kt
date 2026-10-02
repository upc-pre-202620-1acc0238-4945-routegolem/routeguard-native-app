package pe.edu.upc.routeguard.core.network.mapbox

import com.google.gson.annotations.SerializedName

// ---- Directions API ----

data class DirectionsResponseDto(
    val code: String?,
    val routes: List<DirectionsRouteDto>?
)

data class DirectionsRouteDto(
    val distance: Double,
    val duration: Double,
    val geometry: LineGeometryDto?
)

data class LineGeometryDto(
    /** GeoJSON order: [longitude, latitude]. */
    val coordinates: List<List<Double>>?
)

// ---- Optimization API ----

data class OptimizationResponseDto(
    val code: String?,
    val waypoints: List<OptimizationWaypointDto>?
)

data class OptimizationWaypointDto(
    @SerializedName("waypoint_index")
    val waypointIndex: Int
)

// ---- Geocoding API v6 ----

data class GeocodingResponseDto(
    val features: List<GeocodingFeatureDto>?
)

data class GeocodingFeatureDto(
    val properties: GeocodingPropertiesDto?,
    val geometry: PointGeometryDto?
)

data class GeocodingPropertiesDto(
    val name: String?,
    @SerializedName("full_address")
    val fullAddress: String?,
    @SerializedName("place_formatted")
    val placeFormatted: String?
)

data class PointGeometryDto(
    /** GeoJSON order: [longitude, latitude]. */
    val coordinates: List<Double>?
)
