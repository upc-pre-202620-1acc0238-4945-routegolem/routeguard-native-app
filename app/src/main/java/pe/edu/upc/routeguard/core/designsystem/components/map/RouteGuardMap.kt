package pe.edu.upc.routeguard.core.designsystem.components.map

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.gestures.gestures
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.shared.domain.Coordinates

private val StopColor = Color(0xFF006A65)
private val ReachedColor = Color(0xFF9E9E9E)
private val NextStopColor = Color(0xFFEF6C00)
private val VehicleColor = Color(0xFF1565C0)
private val SelectedVehicleColor = Color(0xFFC62828)
private val PickedColor = Color(0xFF6A1B9A)
private val DefaultCenter = Coordinates(-12.0464, -77.0428)

/** A stop of the route drawn on the map. */
data class MapStop(
    val name: String,
    val coordinates: Coordinates,
    val order: Int,
    val reached: Boolean = false,
    val isNext: Boolean = false
)

/** A vehicle (driver position) drawn on the map. */
data class MapVehicle(
    val id: String,
    val label: String,
    val coordinates: Coordinates,
    val selected: Boolean = false
)

private fun Coordinates.toPoint(): Point = Point.fromLngLat(longitude, latitude)

private fun fitCamera(mapView: MapView, points: List<Coordinates>) {
    if (points.isEmpty()) return
    val mapboxMap = mapView.mapboxMap
    if (points.size == 1) {
        mapboxMap.setCamera(cameraOptions {
            center(points.first().toPoint())
            zoom(15.0)
        })
        return
    }
    val camera = mapboxMap.cameraForCoordinates(
        points.map { it.toPoint() },
        cameraOptions { },
        EdgeInsets(90.0, 60.0, 90.0, 60.0),
        null,
        null
    )
    mapboxMap.setCamera(camera)
}

/**
 * Mapbox adapter shared by every role: the route (stops + path), the vehicles, an optional picked
 * point and, when [onMapClick] is set, taps on the map. The camera frames all the content and can
 * follow a vehicle. Without a public token (MAPBOX_ACCESS_TOKEN in local.properties) it shows a
 * hint instead of the map, so the rest of the screen keeps working.
 *
 * @param routePath road path through the stops (Directions API); straight lines when null.
 * @param fitKey changing this value frames the content again (use it when the data arrives).
 */
@Composable
fun RouteGuardMap(
    stops: List<MapStop>,
    modifier: Modifier = Modifier,
    vehicles: List<MapVehicle> = emptyList(),
    height: Dp = 280.dp,
    followVehicleId: String? = null,
    routePath: List<Coordinates>? = null,
    pickedPoint: Coordinates? = null,
    onMapClick: ((Coordinates) -> Unit)? = null,
    fitKey: Any? = Unit
) {
    val token = LocalContext.current.getString(R.string.mapbox_access_token)
    val frame = modifier
        .fillMaxWidth()
        .height(height)
        .clip(RoundedCornerShape(16.dp))

    if (!token.startsWith("pk.")) {
        Box(modifier = frame, contentAlignment = Alignment.Center) {
            Text(
                text = "Mapa desactivado: agrega MAPBOX_ACCESS_TOKEN=pk... en local.properties y vuelve a compilar.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    val orderedStops = remember(stops) { stops.sortedBy { it.order } }
    val allPoints = orderedStops.map { it.coordinates } + vehicles.map { it.coordinates } + listOfNotNull(pickedPoint)
    val latestPoints by rememberUpdatedState(allPoints)
    val clickCallback by rememberUpdatedState(onMapClick)

    val followed = vehicles.firstOrNull { it.id == followVehicleId }
    var following by remember(followVehicleId) { mutableStateOf(followVehicleId != null) }
    var refitSignal by remember { mutableIntStateOf(0) }

    val viewport = rememberMapViewportState {
        setCameraOptions {
            center((allPoints.firstOrNull() ?: DefaultCenter).toPoint())
            zoom(12.0)
        }
    }

    // The camera follows the vehicle while "following" is on.
    LaunchedEffect(followed?.coordinates, following) {
        if (following) followed?.let { viewport.easeTo(cameraOptions { center(it.coordinates.toPoint()) }) }
    }

    val transparentIcon = remember { IconImage(Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)) }

    Box(modifier = frame) {
        MapboxMap(modifier = Modifier.fillMaxSize(), mapViewportState = viewport) {
            MapEffect(fitKey, refitSignal) { mapView -> fitCamera(mapView, latestPoints) }

            MapEffect(Unit) { mapView ->
                mapView.gestures.addOnMapClickListener { point ->
                    val callback = clickCallback
                    if (callback != null) {
                        callback(Coordinates(point.latitude(), point.longitude()))
                        true
                    } else {
                        false
                    }
                }
            }

            // Along the roads when Mapbox answered; straight lines between stops otherwise.
            val linePoints = routePath?.takeIf { it.size >= 2 } ?: orderedStops.map { it.coordinates }
            if (linePoints.size >= 2) {
                PolylineAnnotation(points = linePoints.map { it.toPoint() }) {
                    lineColor = StopColor
                    lineWidth = 4.0
                }
            }

            orderedStops.forEach { stop ->
                CircleAnnotation(point = stop.coordinates.toPoint()) {
                    circleRadius = if (stop.isNext) 11.0 else 8.0
                    circleColor = when {
                        stop.isNext -> NextStopColor
                        stop.reached -> ReachedColor
                        else -> StopColor
                    }
                    circleStrokeColor = Color.White
                    circleStrokeWidth = 2.0
                }
                PointAnnotation(point = stop.coordinates.toPoint()) {
                    iconImage = transparentIcon
                    textField = "${stop.order}. ${stop.name}"
                    textSize = 12.0
                    textColor = Color.Black
                    textHaloColor = Color.White
                    textHaloWidth = 1.5
                    textOffset = listOf(0.0, 1.8)
                }
            }

            pickedPoint?.let {
                CircleAnnotation(point = it.toPoint()) {
                    circleRadius = 10.0
                    circleColor = PickedColor
                    circleStrokeColor = Color.White
                    circleStrokeWidth = 3.0
                }
            }

            vehicles.forEach { vehicle ->
                CircleAnnotation(point = vehicle.coordinates.toPoint()) {
                    circleRadius = if (vehicle.selected) 13.0 else 10.0
                    circleColor = if (vehicle.selected) SelectedVehicleColor else VehicleColor
                    circleStrokeColor = Color.White
                    circleStrokeWidth = 3.0
                }
                PointAnnotation(point = vehicle.coordinates.toPoint()) {
                    iconImage = transparentIcon
                    textField = vehicle.label
                    textSize = 12.0
                    textColor = Color.Black
                    textHaloColor = Color.White
                    textHaloWidth = 1.5
                    textOffset = listOf(0.0, -1.8)
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (followed != null) {
                FilledTonalButton(onClick = { following = true }, enabled = !following) {
                    Text("Seguir")
                }
            }
            FilledTonalButton(onClick = {
                following = false
                refitSignal++
            }) {
                Text("Ver todo")
            }
        }
    }
}
