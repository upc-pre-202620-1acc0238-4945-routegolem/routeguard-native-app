package pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.route

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.FormField
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
import pe.edu.upc.routeguard.core.designsystem.components.InfoText
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.SecondaryButton
import pe.edu.upc.routeguard.core.designsystem.components.SectionTitle
import pe.edu.upc.routeguard.core.designsystem.components.map.MapStop
import pe.edu.upc.routeguard.core.designsystem.components.map.MapVehicle
import pe.edu.upc.routeguard.core.designsystem.components.map.RouteGuardMap
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.BoardingState
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.IncidentType
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.NextStop
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Trip
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.progressText
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripStatus
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Waypoint
import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.StudentId

private const val LOCATION_REFRESH_MS = 5_000L

private val Green = Color(0xFF047857)
private val Red = Color(0xFFB91C1C)

/** Driver home: route cards, one-touch boarding and background GPS transmission. */
@Composable
fun RouteMapScreen(
    modifier: Modifier = Modifier,
    viewModel: TrackingViewModel = hiltViewModel(),
    onOpenAlerts: () -> Unit = {}
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val trip = state.trip

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.onLocationPermissionResult(result[Manifest.permission.ACCESS_FINE_LOCATION] == true)
    }

    LaunchedEffect(Unit) { viewModel.refresh() }

    // Live position of the driver: polls the last point saved by the background GPS.
    LaunchedEffect(trip?.id, trip?.status) {
        while (trip?.isActive == true) {
            viewModel.refreshLocation()
            delay(LOCATION_REFRESH_MS)
        }
    }

    // Background GPS starts once the trip is in progress and stops when it is over.
    LaunchedEffect(trip?.status) {
        val hasLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (trip?.acceptsRecords == true && !hasLocation) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions += Manifest.permission.POST_NOTIFICATIONS
            }
            permissionLauncher.launch(permissions.toTypedArray())
        } else {
            viewModel.onLocationPermissionResult(hasLocation)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = trip?.routeName?.ifBlank { null } ?: "Mi ruta")

        if (state.isLoading && trip == null && state.assignedRoutes.isEmpty()) {
            LoadingBox()
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                InfoText(state.message)
                ErrorText(state.errorMessage)
            }

            if (trip == null) {
                item { SectionTitle("Elige la ruta de hoy") }
                if (state.assignedRoutes.isEmpty()) {
                    item { Text("No tienes rutas activas asignadas.") }
                }
                items(state.assignedRoutes, key = { it.routeId.value }) { route ->
                    InfoCard(
                        title = route.name,
                        subtitle = "${route.stopCount} paradas · ${route.studentCount} estudiantes" +
                            (route.departureTime?.let { " · sale $it" } ?: "")
                    ) {
                        BigButton(
                            text = "Iniciar viaje",
                            onClick = { viewModel.onStartTrip(route.routeId) },
                            enabled = !state.isLoading
                        )
                    }
                }
            } else {
                tripContent(
                    trip = trip,
                    pendingRecords = state.pendingRecords,
                    currentLocation = state.currentLocation,
                    visitedStops = state.visitedStops,
                    roadPath = state.roadPath,
                    nextStop = state.nextStop,
                    enabled = !state.isLoading,
                    onBoardingStatus = viewModel::onBoardingStatus,
                    onSync = viewModel::onSyncOffline,
                    onReportIncident = viewModel::onReportIncident,
                    onFinish = viewModel::onFinishTrip
                )
            }

            item {
                SecondaryButton(text = "Alertas y avisos", onClick = onOpenAlerts)
            }
        }
    }
}

private fun LazyListScope.tripContent(
    trip: Trip,
    pendingRecords: Int,
    currentLocation: Coordinates?,
    visitedStops: Set<Int>,
    roadPath: List<Coordinates>?,
    nextStop: NextStop?,
    enabled: Boolean,
    onBoardingStatus: (StudentId, BoardingState) -> Unit,
    onSync: () -> Unit,
    onReportIncident: (IncidentType, String) -> Unit,
    onFinish: () -> Unit
) {
    item {
        Text(
            text = when (trip.status) {
                TripStatus.PENDING -> "Viaje pendiente"
                TripStatus.IN_PROGRESS -> "Viaje en curso · ${trip.boardedCount} a bordo · " +
                    "${trip.absentCount} ausentes · ${trip.waypoints.size} en total"
                TripStatus.COMPLETED -> "Viaje finalizado"
            },
            style = MaterialTheme.typography.titleMedium
        )
    }

    item {
        RouteGuardMap(
            stops = trip.stops.map {
                MapStop(it.name, it.coordinates, it.order, it.order in visitedStops, it.order == nextStop?.order)
            },
            vehicles = currentLocation?.let { listOf(MapVehicle("me", "Yo", it)) }.orEmpty(),
            followVehicleId = "me",
            routePath = roadPath,
            fitKey = trip.id.value
        )
    }

    item { NextStopCard(nextStop) }

    if (trip.stops.isNotEmpty()) {
        item {
            InfoCard(title = "Paradas de la ruta") {
                trip.stops.forEach { Text("${it.order}. ${it.name}") }
            }
        }
    }

    items(trip.waypoints, key = { it.id.value }) { waypoint ->
        WaypointCard(
            waypoint = waypoint,
            canRecord = trip.acceptsRecords && enabled,
            onBoardingStatus = onBoardingStatus
        )
    }

    item {
        if (pendingRecords > 0) {
            Text("$pendingRecords registros pendientes de sincronizar")
        }
        SecondaryButton(text = "Sincronizar ahora", onClick = onSync)
    }

    item { IncidentButton(onReportIncident) }

    item {
        BigButton(
            text = "Finalizar ruta",
            onClick = onFinish,
            enabled = enabled && trip.isActive,
            containerColor = Red
        )
    }
}

@Composable
private fun NextStopCard(nextStop: NextStop?) {
    val context = LocalContext.current
    if (nextStop == null) {
        InfoCard(title = "Recorrido completo", subtitle = "Ya pasaste por todas las paradas.")
        return
    }
    InfoCard(title = "Próxima parada: ${nextStop.name}", subtitle = nextStop.progressText()) {
        SecondaryButton(text = "Navegar con Google Maps", onClick = { openNavigation(context, nextStop) })
    }
}

/** Hands the next stop to Google Maps turn-by-turn navigation (any maps app as a fallback). */
private fun openNavigation(context: Context, stop: NextStop) {
    val lat = stop.coordinates.latitude
    val lng = stop.coordinates.longitude
    val navigation = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$lat,$lng&mode=d"))
        .setPackage("com.google.android.apps.maps")
    try {
        context.startActivity(navigation)
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(stop.name)})"))
            )
        } catch (_: ActivityNotFoundException) {
            // No maps application installed.
        }
    }
}

@Composable
private fun WaypointCard(
    waypoint: Waypoint,
    canRecord: Boolean,
    onBoardingStatus: (StudentId, BoardingState) -> Unit
) {
    InfoCard(title = waypoint.studentName) {
        when (waypoint.status) {
            BoardingState.MISSING -> {
                BigButton(
                    text = "Check-in",
                    onClick = { onBoardingStatus(waypoint.studentId, BoardingState.BOARDED) },
                    enabled = canRecord,
                    containerColor = Green
                )
                SecondaryButton(
                    text = "No asistió",
                    onClick = { onBoardingStatus(waypoint.studentId, BoardingState.OMITTED) },
                    enabled = canRecord
                )
            }

            BoardingState.BOARDED -> Text("A bordo", color = Green)

            BoardingState.OMITTED -> {
                Text("No asistió")
                SecondaryButton(
                    text = "Check-in",
                    onClick = { onBoardingStatus(waypoint.studentId, BoardingState.BOARDED) },
                    enabled = canRecord
                )
            }
        }
    }
}

@Composable
private fun IncidentButton(onReportIncident: (IncidentType, String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(IncidentType.TRAFFIC) }
    var description by remember { mutableStateOf("") }

    SecondaryButton(text = "Reportar incidencia", onClick = { showDialog = true })

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Reportar incidencia") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    IncidentType.entries.forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = { Text(option.label) }
                        )
                    }
                    FormField(description, { description = it }, "Detalle", singleLine = false)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onReportIncident(type, description)
                    description = ""
                    showDialog = false
                }) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
