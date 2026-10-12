package pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.follow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.map.MapStop
import pe.edu.upc.routeguard.core.designsystem.components.map.MapVehicle
import pe.edu.upc.routeguard.core.designsystem.components.map.RouteGuardMap
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.withEstimate
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.formatAge
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.parentLabel
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.progressText

private const val REFRESH_MS = 5_000L
private const val VEHICLE_ID = "vehicle"

/** Parent view: where is the vehicle that carries my children, and how far is the next stop. */
@Composable
fun FollowTripScreen(
    modifier: Modifier = Modifier,
    viewModel: FollowTripViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    // Polls while the screen is visible.
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refresh()
            delay(REFRESH_MS)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = "Seguimiento")

        val trip = state.trip
        when {
            !state.hasLoaded -> LoadingBox()

            trip == null -> Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ErrorText(state.errorMessage)
                InfoCard(
                    title = "Sin viaje en curso",
                    subtitle = "Cuando el conductor inicie la ruta de tu hijo, verás aquí dónde está el vehículo."
                )
            }

            else -> Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val nextStop = trip.nextStop?.withEstimate(state.arrival)
                RouteGuardMap(
                    stops = trip.stops.map {
                        MapStop(it.name, it.coordinates, it.order, it.reached, it.order == nextStop?.order)
                    },
                    vehicles = trip.location?.let {
                        listOf(MapVehicle(VEHICLE_ID, "Vehículo", it.coordinates))
                    }.orEmpty(),
                    followVehicleId = VEHICLE_ID,
                    routePath = state.roadPath,
                    height = 320.dp,
                    fitKey = trip.tripId
                )

                ErrorText(state.errorMessage)

                InfoCard(
                    title = trip.routeName,
                    subtitle = "Conductor: ${trip.driverName.ifBlank { "—" }}"
                ) {
                    val location = trip.location
                    if (location != null) {
                        Text("Velocidad: ${location.speedKmh.toInt()} km/h · ${formatAge(location.recordedAt)}")
                    } else {
                        Text("Esperando la primera señal GPS del vehículo")
                    }
                }

                InfoCard(title = "Próxima parada") {
                    Text(
                        text = if (nextStop == null) {
                            "El vehículo ya pasó por todas las paradas"
                        } else {
                            "${nextStop.name} · ${nextStop.progressText()}"
                        }
                    )
                }

                InfoCard(title = "Tus hijos") {
                    if (trip.children.isEmpty()) Text("Ninguno de tus hijos viaja en esta ruta")
                    trip.children.forEach { child ->
                        Text("• ${child.name}: ${child.state.parentLabel()}")
                    }
                }
            }
        }
    }
}
