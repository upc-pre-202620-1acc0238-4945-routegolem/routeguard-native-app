package pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.monitor

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
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.progressText

private const val REFRESH_MS = 5_000L

/** Administrator monitor: every trip in progress on a single map. */
@Composable
fun LiveMonitorScreen(
    modifier: Modifier = Modifier,
    viewModel: LiveMonitorViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refresh()
            delay(REFRESH_MS)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = "Flota en vivo")

        if (!state.hasLoaded) {
            LoadingBox()
            return@Column
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ErrorText(state.errorMessage)

            val selected = state.selectedTrip
            if (selected == null) {
                InfoCard(
                    title = "Sin viajes en curso",
                    subtitle = "Cuando un conductor inicie su ruta, verás aquí su vehículo en el mapa."
                )
                return@Column
            }

            val nextStopOrder = selected.nextStop?.order
            RouteGuardMap(
                stops = selected.stops.map {
                    MapStop(it.name, it.coordinates, it.order, it.reached, it.order == nextStopOrder)
                },
                vehicles = state.trips.mapNotNull { trip ->
                    trip.location?.let {
                        MapVehicle(
                            id = trip.tripId.value,
                            label = trip.driverName.ifBlank { trip.routeName },
                            coordinates = it.coordinates,
                            selected = trip.tripId == selected.tripId
                        )
                    }
                },
                followVehicleId = selected.tripId.value,
                routePath = state.roadPath,
                height = 320.dp,
                fitKey = selected.tripId.value to state.trips.size
            )

            state.trips.forEach { trip ->
                val next = trip.nextStop?.let {
                    if (trip.tripId == selected.tripId) it.withEstimate(state.arrival) else it
                }
                InfoCard(
                    title = trip.routeName,
                    subtitle = "Conductor: ${trip.driverName.ifBlank { "—" }}",
                    onClick = { viewModel.onTripSelected(trip.tripId.value) }
                ) {
                    Text("${trip.boardedCount} de ${trip.totalChildren} estudiantes a bordo")
                    Text(
                        text = trip.location?.let {
                            "${it.speedKmh.toInt()} km/h · ${formatAge(it.recordedAt)}"
                        } ?: "Sin señal GPS todavía"
                    )
                    Text(text = next?.let { "Próxima parada: ${it.name} · ${it.progressText()}" } ?: "Recorrido completo")
                    if (trip.tripId == selected.tripId) Text("Seleccionado en el mapa")
                }
            }
        }
    }
}
