package pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.route

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.FormField
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
import pe.edu.upc.routeguard.core.designsystem.components.InfoText
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.SecondaryButton
import pe.edu.upc.routeguard.core.designsystem.components.SectionTitle
import pe.edu.upc.routeguard.core.designsystem.components.StatusChip
import pe.edu.upc.routeguard.core.designsystem.components.map.MapStop
import pe.edu.upc.routeguard.core.designsystem.components.map.MapVehicle
import pe.edu.upc.routeguard.core.designsystem.components.map.RouteGuardMap
import pe.edu.upc.routeguard.core.designsystem.theme.BrandBorder
import pe.edu.upc.routeguard.core.designsystem.theme.BrandCardWhite
import pe.edu.upc.routeguard.core.designsystem.theme.BrandEmerald
import pe.edu.upc.routeguard.core.designsystem.theme.BrandEmeraldDark
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy
import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.BoardingState
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.IncidentType
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.NextStop
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Trip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripStatus
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Waypoint
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.StudentId
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.progressText

private const val LOCATION_REFRESH_MS = 5_000L
private val Red = Color(0xFFBA1A1A)

/** Driver home: route cards, one-touch boarding checklist and background GPS transmission. */
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            enabled = !state.isLoading,
                            containerColor = BrandNavy
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
                    onArriveAtSchool = viewModel::onArriveAtSchool,
                    onSync = viewModel::onSyncOffline,
                    onReportIncident = viewModel::onReportIncident,
                    onFinish = viewModel::onFinishTrip
                )
            }

            item {
                SecondaryButton(text = "Alertas y avisos", onClick = onOpenAlerts)
                Spacer(modifier = Modifier.height(16.dp))
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
    onArriveAtSchool: () -> Unit,
    onSync: () -> Unit,
    onReportIncident: (IncidentType, String) -> Unit,
    onFinish: () -> Unit
) {
    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
            border = BorderStroke(1.dp, BrandBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (trip.status) {
                            TripStatus.PENDING -> "Viaje pendiente"
                            TripStatus.IN_PROGRESS -> "Viaje en curso"
                            TripStatus.COMPLETED -> "Viaje finalizado"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                    StatusChip(
                        text = if (trip.isActive) "En movimiento" else "Finalizado",
                        containerColor = if (trip.isActive) Color(0xFFD1FAE5) else Color(0xFFF1F5F9),
                        contentColor = if (trip.isActive) Color(0xFF064E3B) else Color(0xFF64748B),
                        icon = if (trip.isActive) Icons.Filled.Check else null
                    )
                }

                if (trip.status == TripStatus.IN_PROGRESS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip(
                            text = "${trip.boardedCount} a bordo",
                            containerColor = Color(0xFFD1FAE5),
                            contentColor = Color(0xFF064E3B),
                            icon = Icons.Filled.Check
                        )
                        StatusChip(
                            text = "${trip.absentCount} ausentes",
                            containerColor = Color(0xFFFFDAD6),
                            contentColor = Color(0xFFBA1A1A),
                            icon = Icons.Filled.Close
                        )
                        StatusChip(
                            text = "${trip.waypoints.size} en total",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            icon = Icons.Filled.Place
                        )
                    }
                }
            }
        }
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
                trip.stops.forEach {
                    Text(
                        text = "${it.order}. ${it.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }

    item {
        BigButton(
            text = "Llegada al colegio (Descenso general)",
            onClick = onArriveAtSchool,
            enabled = enabled && trip.isActive,
            containerColor = BrandEmeraldDark
        )
    }

    item {
        SectionTitle(text = "Checklist de Pasajeros (${trip.waypoints.size})")
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
            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Hay $pendingRecords registros pendientes de sincronizar con el servidor",
                    color = Color(0xFF92400E),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
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
        Spacer(modifier = Modifier.height(4.dp))
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
    val initials = waypoint.studentName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifBlank { "ST" }

    val statusChipContainer = when (waypoint.status) {
        BoardingState.BOARDED -> Color(0xFFD1FAE5)
        BoardingState.OMITTED -> Color(0xFFFFDAD6)
        BoardingState.MISSING -> Color(0xFFF1F5F9)
    }

    val statusChipContent = when (waypoint.status) {
        BoardingState.BOARDED -> Color(0xFF064E3B)
        BoardingState.OMITTED -> Color(0xFFBA1A1A)
        BoardingState.MISSING -> Color(0xFF475569)
    }

    val statusText = when (waypoint.status) {
        BoardingState.BOARDED -> "A bordo"
        BoardingState.OMITTED -> "No asistió"
        BoardingState.MISSING -> "Pendiente"
    }

    val statusIcon = when (waypoint.status) {
        BoardingState.BOARDED -> Icons.Filled.Check
        BoardingState.OMITTED -> Icons.Filled.Close
        BoardingState.MISSING -> Icons.Filled.Place
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BrandBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            when (waypoint.status) {
                                BoardingState.BOARDED -> Color(0xFFD1FAE5)
                                BoardingState.OMITTED -> Color(0xFFFFDAD6)
                                BoardingState.MISSING -> BrandNavy.copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (waypoint.status) {
                            BoardingState.BOARDED -> Color(0xFF064E3B)
                            BoardingState.OMITTED -> Color(0xFFBA1A1A)
                            BoardingState.MISSING -> BrandNavy
                        }
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = waypoint.studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Parada de recojo escolar",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
                StatusChip(
                    text = statusText,
                    containerColor = statusChipContainer,
                    contentColor = statusChipContent,
                    icon = statusIcon
                )
            }

            // Actions row
            when (waypoint.status) {
                BoardingState.MISSING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onBoardingStatus(waypoint.studentId, BoardingState.BOARDED) },
                            enabled = canRecord,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandEmeraldDark),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Subió", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onBoardingStatus(waypoint.studentId, BoardingState.OMITTED) },
                            enabled = canRecord,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("No asistió", color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                BoardingState.OMITTED -> {
                    TextButton(
                        onClick = { onBoardingStatus(waypoint.studentId, BoardingState.BOARDED) },
                        enabled = canRecord,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = BrandEmeraldDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rectificar: Marcar como Subió", color = BrandEmeraldDark, fontWeight = FontWeight.Bold)
                    }
                }
                BoardingState.BOARDED -> {
                    // Status badge already indicates "A bordo"
                }
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
            title = { Text("Reportar incidencia", fontWeight = FontWeight.Bold) },
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
                }) { Text("Enviar", fontWeight = FontWeight.Bold, color = BrandNavy) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
