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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.FormField
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
import pe.edu.upc.routeguard.core.designsystem.components.InfoText
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.MaintenanceHistoryDialog
import pe.edu.upc.routeguard.core.designsystem.components.OperationalExpensesDialog
import pe.edu.upc.routeguard.core.designsystem.components.PickupAndOptimizationDialog
import pe.edu.upc.routeguard.core.designsystem.components.RouteAnnouncementsDialog
import pe.edu.upc.routeguard.core.designsystem.components.RouteChatDialog
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.SecondaryButton
import pe.edu.upc.routeguard.core.designsystem.components.SectionTitle
import pe.edu.upc.routeguard.core.designsystem.components.StatusChip
import pe.edu.upc.routeguard.core.designsystem.components.VehicleDocsDialog
import pe.edu.upc.routeguard.core.designsystem.components.VehicleDocsStatusBadges
import pe.edu.upc.routeguard.core.designsystem.components.VehicleDocumentation
import pe.edu.upc.routeguard.core.presentation.PlatformFeaturesViewModel
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
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.AttendanceReportDialog
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.AttendanceReportFormatter
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.TripHistoryDialog
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.localizedProgressText

private const val LOCATION_REFRESH_MS = 5_000L
private val Red = Color(0xFFBA1A1A)

/** Driver home: route cards, one-touch boarding checklist and background GPS transmission. */
@Composable
fun RouteMapScreen(
    modifier: Modifier = Modifier,
    viewModel: TrackingViewModel = hiltViewModel(),
    platformViewModel: PlatformFeaturesViewModel = hiltViewModel(),
    onOpenAlerts: () -> Unit = {}
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val platformState = platformViewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val trip = state.trip
    var showTripHistoryDialog by remember { mutableStateOf(false) }
    var showDocsDialog by remember { mutableStateOf(false) }
    var showAttendanceDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showAnnouncementsDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var showMaintenanceDialog by remember { mutableStateOf(false) }
    var showExpensesDialog by remember { mutableStateOf(false) }
    var showOptimizationDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.onLocationPermissionResult(result[Manifest.permission.ACCESS_FINE_LOCATION] == true)
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
        platformViewModel.refreshProfileAndDocs()
        platformViewModel.refreshOperations()
    }

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
        ScreenHeader(title = trip?.routeName?.ifBlank { null } ?: stringResource(R.string.route_map_title))

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
                item { SectionTitle(stringResource(R.string.route_map_pick_route)) }
                if (state.assignedRoutes.isEmpty()) {
                    item { Text(stringResource(R.string.route_map_no_routes)) }
                }
                items(state.assignedRoutes, key = { it.routeId.value }) { route ->
                    InfoCard(
                        title = route.name,
                        subtitle = stringResource(R.string.route_map_stops_students, route.stopCount, route.studentCount) +
                            (route.departureTime?.let { stringResource(R.string.route_map_departure_inline, it) } ?: "")
                    ) {
                        BigButton(
                            text = stringResource(R.string.route_map_start_trip),
                            onClick = { viewModel.onStartTrip(route.routeId) },
                            enabled = !state.isLoading,
                            containerColor = BrandNavy
                        )
                    }
                }
            } else {
                tripContent(
                    trip = trip,
                    cancellationReason = state.cancellationReason,
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
                    onOpenAttendanceReport = { showAttendanceDialog = true },
                    onOpenDocs = { showDocsDialog = true },
                    onOpenCancelTrip = { showCancelDialog = true },
                    onFinish = viewModel::onFinishTrip
                )
            }

            item {
                SecondaryButton(
                    text = stringResource(R.string.trip_history_btn),
                    onClick = {
                        viewModel.loadTripHistory()
                        showTripHistoryDialog = true
                    },
                    icon = Icons.Filled.DateRange
                )
            }

            item {
                SecondaryButton(
                    text = stringResource(R.string.announcements_btn),
                    onClick = {
                        platformViewModel.refreshOperations(trip?.routeId?.value ?: "f0000000-0000-0000-0000-000000000001")
                        showAnnouncementsDialog = true
                    },
                    icon = Icons.Filled.List
                )
            }

            item {
                SecondaryButton(
                    text = stringResource(R.string.chat_btn),
                    onClick = {
                        platformViewModel.refreshOperations(trip?.routeId?.value ?: "f0000000-0000-0000-0000-000000000001")
                        showChatDialog = true
                    },
                    icon = Icons.Filled.Place
                )
            }

            item {
                SecondaryButton(
                    text = stringResource(R.string.pickup_opt_btn),
                    onClick = {
                        platformViewModel.refreshOperations(trip?.routeId?.value ?: "f0000000-0000-0000-0000-000000000001")
                        showOptimizationDialog = true
                    },
                    icon = Icons.Filled.List
                )
            }

            item {
                SecondaryButton(
                    text = stringResource(R.string.maintenances_btn),
                    onClick = {
                        platformViewModel.refreshOperations()
                        showMaintenanceDialog = true
                    },
                    icon = Icons.Filled.List
                )
            }

            item {
                SecondaryButton(
                    text = stringResource(R.string.expenses_btn),
                    onClick = {
                        platformViewModel.refreshOperations()
                        showExpensesDialog = true
                    },
                    icon = Icons.Filled.List
                )
            }

            item {
                SecondaryButton(text = stringResource(R.string.route_map_alerts_notices_btn), onClick = onOpenAlerts)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showTripHistoryDialog) {
        TripHistoryDialog(
            trips = state.tripHistory,
            isLoading = state.isHistoryLoading,
            onDismiss = { showTripHistoryDialog = false }
        )
    }

    if (showDocsDialog) {
        val vCache = platformState.primaryVehicleDocs
        VehicleDocsDialog(
            docs = VehicleDocumentation(
                plate = vCache?.plate ?: "ABC-123",
                unitModel = vCache?.model ?: "Mercedes-Benz Sprinter 416",
                soatPolicy = vCache?.soatPolicy ?: "SOAT-2026-88412",
                soatInsurer = vCache?.soatInsurer ?: stringResource(R.string.docs_default_insurer),
                soatExpiry = vCache?.soatExpiresAt ?: "2026-12-15"
            ),
            onDismiss = { showDocsDialog = false }
        )
    }

    if (showAnnouncementsDialog) {
        RouteAnnouncementsDialog(
            announcements = platformState.announcements,
            canPublish = true,
            onPublish = { title, msg, prio ->
                platformViewModel.publishAnnouncement(trip?.routeId?.value, title, msg, prio)
            },
            onDismiss = { showAnnouncementsDialog = false }
        )
    }

    if (showChatDialog) {
        RouteChatDialog(
            messages = platformState.chatMessages,
            currentUserId = "",
            onSend = { msg -> platformViewModel.sendChatMessage(trip?.routeId?.value, msg) },
            onDismiss = { showChatDialog = false }
        )
    }

    if (showOptimizationDialog) {
        PickupAndOptimizationDialog(
            canRequestChange = false,
            canApproveChanges = true,
            defaultChildName = "",
            pickupChanges = platformState.pickupChanges,
            optimizedRoute = platformState.optimizedRoute,
            onRequestChange = { _, _, _, _ -> },
            onReviewChange = { id, status -> platformViewModel.reviewPickupChange(id, status) },
            onDismiss = { showOptimizationDialog = false }
        )
    }

    if (showMaintenanceDialog) {
        MaintenanceHistoryDialog(
            maintenances = platformState.maintenances,
            canAdd = true,
            defaultPlate = platformState.primaryVehicleDocs?.plate ?: "ABC-123",
            onAdd = { plate, type, desc, shop, cost, km ->
                platformViewModel.addMaintenance(plate, type, desc, shop, cost, km)
            },
            onDismiss = { showMaintenanceDialog = false }
        )
    }

    if (showExpensesDialog) {
        OperationalExpensesDialog(
            expenses = platformState.expenses,
            defaultPlate = platformState.primaryVehicleDocs?.plate ?: "ABC-123",
            onAdd = { plate, cat, desc, amount ->
                platformViewModel.addExpense(plate, cat, desc, amount)
            },
            onDismiss = { showExpensesDialog = false }
        )
    }

    if (showAttendanceDialog && trip != null) {
        AttendanceReportDialog(
            routeName = trip.routeName,
            summary = AttendanceReportFormatter.fromTrip(trip),
            onDismiss = { showAttendanceDialog = false }
        )
    }

    if (showCancelDialog && trip != null) {
        ForceMajeureCancelDialog(
            onDismiss = { showCancelDialog = false },
            onConfirm = { reason ->
                viewModel.onCancelTrip(reason)
                showCancelDialog = false
            }
        )
    }
}

private fun LazyListScope.tripContent(
    trip: Trip,
    cancellationReason: String?,
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
    onOpenAttendanceReport: () -> Unit,
    onOpenDocs: () -> Unit,
    onOpenCancelTrip: () -> Unit,
    onFinish: () -> Unit
) {
    if (trip.status == TripStatus.CANCELLED) {
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFFDAD6),
                border = BorderStroke(1.dp, Red.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Red, modifier = Modifier.size(22.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.cancel_trip_banner_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Red
                        )
                        Text(
                            text = stringResource(
                                R.string.cancel_trip_banner_body,
                                cancellationReason ?: stringResource(R.string.cancel_reason_road_closure)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF410002)
                        )
                    }
                }
            }
        }
    }

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
                            TripStatus.PENDING -> stringResource(R.string.route_map_trip_pending)
                            TripStatus.IN_PROGRESS -> stringResource(R.string.route_map_trip_in_progress)
                            TripStatus.COMPLETED -> stringResource(R.string.route_map_trip_completed)
                            TripStatus.CANCELLED -> stringResource(R.string.route_map_trip_cancelled)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                    StatusChip(
                        text = when (trip.status) {
                            TripStatus.IN_PROGRESS -> stringResource(R.string.route_map_moving)
                            TripStatus.CANCELLED -> stringResource(R.string.route_map_cancelled_chip)
                            else -> stringResource(R.string.route_map_finished_chip)
                        },
                        containerColor = when (trip.status) {
                            TripStatus.IN_PROGRESS -> Color(0xFFD1FAE5)
                            TripStatus.CANCELLED -> Color(0xFFFFDAD6)
                            else -> Color(0xFFF1F5F9)
                        },
                        contentColor = when (trip.status) {
                            TripStatus.IN_PROGRESS -> Color(0xFF064E3B)
                            TripStatus.CANCELLED -> Red
                            else -> Color(0xFF64748B)
                        },
                        icon = when (trip.status) {
                            TripStatus.IN_PROGRESS -> Icons.Filled.Check
                            TripStatus.CANCELLED -> Icons.Filled.Close
                            else -> null
                        }
                    )
                }

                if (trip.status == TripStatus.IN_PROGRESS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip(
                            text = stringResource(R.string.route_map_boarded_chip, trip.boardedCount),
                            containerColor = Color(0xFFD1FAE5),
                            contentColor = Color(0xFF064E3B),
                            icon = Icons.Filled.Check
                        )
                        StatusChip(
                            text = stringResource(R.string.route_map_omitted_chip, trip.absentCount),
                            containerColor = Color(0xFFFFDAD6),
                            contentColor = Color(0xFFBA1A1A),
                            icon = Icons.Filled.Close
                        )
                        StatusChip(
                            text = stringResource(R.string.route_map_total_chip, trip.waypoints.size),
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
        val meLabel = stringResource(R.string.route_map_me_marker)
        RouteGuardMap(
            stops = trip.stops.map {
                MapStop(it.name, it.coordinates, it.order, it.order in visitedStops, it.order == nextStop?.order)
            },
            vehicles = currentLocation?.let { listOf(MapVehicle("me", meLabel, it)) }.orEmpty(),
            followVehicleId = "me",
            routePath = roadPath,
            fitKey = trip.id.value
        )
    }

    item { NextStopCard(nextStop) }

    if (trip.stops.isNotEmpty()) {
        item {
            InfoCard(title = stringResource(R.string.route_map_stops_title)) {
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
            text = stringResource(R.string.route_map_arrived_school_btn),
            onClick = onArriveAtSchool,
            enabled = enabled && trip.isActive,
            containerColor = BrandEmeraldDark
        )
    }

    item {
        SectionTitle(text = stringResource(R.string.route_map_checklist_section, trip.waypoints.size))
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
                    text = stringResource(R.string.route_map_pending_sync_title, pendingRecords),
                    color = Color(0xFF92400E),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        SecondaryButton(text = stringResource(R.string.route_map_sync_now), onClick = onSync)
    }

    item { IncidentButton(enabled = enabled && trip.isActive, onReportIncident = onReportIncident) }

    // US-17: Attendance Report button
    item {
        SecondaryButton(
            text = stringResource(R.string.attendance_report_btn),
            onClick = onOpenAttendanceReport,
            icon = Icons.Filled.List
        )
    }

    // US-12: Driver & Vehicle SOAT / License documentation card
    item {
        InfoCard(title = stringResource(R.string.follow_vehicle_card_title)) {
            VehicleDocsStatusBadges(licenseCode = "A-IIIa")
            OutlinedButton(
                onClick = onOpenDocs,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.docs_view_verify_btn), fontWeight = FontWeight.SemiBold, color = BrandNavy)
            }
        }
    }

    // US-21: Force Majeure Cancellation
    if (trip.isActive) {
        item {
            OutlinedButton(
                onClick = onOpenCancelTrip,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Red)
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = Red, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.cancel_trip_btn),
                    color = Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    item {
        BigButton(
            text = stringResource(R.string.route_map_finish_btn),
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
        InfoCard(
            title = stringResource(R.string.route_map_completed_title),
            subtitle = stringResource(R.string.route_map_completed_sub)
        )
        return
    }
    InfoCard(
        title = stringResource(R.string.route_map_next_stop_title, nextStop.name),
        subtitle = nextStop.localizedProgressText()
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        SecondaryButton(text = stringResource(R.string.route_map_navigate_maps), onClick = { openNavigation(context, nextStop) })
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
        BoardingState.BOARDED -> stringResource(R.string.route_map_boarding_boarded_chip)
        BoardingState.OMITTED -> stringResource(R.string.route_map_boarding_omitted)
        BoardingState.MISSING -> stringResource(R.string.route_map_boarding_missing)
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
                        text = stringResource(R.string.route_map_pickup_stop_sub),
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
                            Text(stringResource(R.string.route_map_boarding_boarded), fontWeight = FontWeight.Bold)
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
                            Text(stringResource(R.string.route_map_boarding_omitted), color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                BoardingState.OMITTED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onBoardingStatus(waypoint.studentId, BoardingState.MISSING) },
                            enabled = canRecord
                        ) {
                            Text(stringResource(R.string.route_map_undo_from_omitted), color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = { onBoardingStatus(waypoint.studentId, BoardingState.BOARDED) },
                            enabled = canRecord
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = BrandEmeraldDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.route_map_rectify_boarded), color = BrandEmeraldDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                BoardingState.BOARDED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onBoardingStatus(waypoint.studentId, BoardingState.MISSING) },
                            enabled = canRecord
                        ) {
                            Text(
                                text = stringResource(R.string.route_map_undo_from_boarded),
                                color = Color(0xFF64748B),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IncidentButton(
    enabled: Boolean,
    onReportIncident: (IncidentType, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(IncidentType.ROADBLOCK) }
    var delayMinutes by remember { mutableIntStateOf(15) }
    var description by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val shortErrorMsg = stringResource(R.string.tracking_error_incident_short)

    SecondaryButton(
        text = stringResource(R.string.route_map_incident_btn),
        onClick = { showDialog = true },
        enabled = enabled,
        icon = Icons.Filled.Warning
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFD97706))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.route_map_incident_title), fontWeight = FontWeight.Bold, color = BrandNavy)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IncidentType.entries.forEach { option ->
                            val isRoadblock = option == IncidentType.ROADBLOCK
                            FilterChip(
                                selected = type == option,
                                onClick = { type = option },
                                leadingIcon = if (isRoadblock) {
                                    {
                                        Icon(
                                            Icons.Filled.Warning,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (type == option) Color.White else Color(0xFFD97706)
                                        )
                                    }
                                } else null,
                                label = {
                                    Text(
                                        stringResource(option.labelRes),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isRoadblock) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (isRoadblock) Color(0xFFD97706) else BrandNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.incident_delay_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandNavy
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 45).forEach { mins ->
                            FilterChip(
                                selected = delayMinutes == mins,
                                onClick = { delayMinutes = mins },
                                label = { Text(stringResource(R.string.incident_delay_option, mins), style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    FormField(
                        value = description,
                        onValueChange = {
                            description = it
                            localError = null
                        },
                        label = stringResource(R.string.route_map_incident_desc_label),
                        singleLine = false
                    )
                    if (localError != null) {
                        Text(
                            text = localError!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = description.trim()
                    val fullDesc = "[+$delayMinutes min] $trimmed".trim()
                    if ("${type.label}: $fullDesc".length < 10 || trimmed.isBlank()) {
                        localError = shortErrorMsg
                    } else {
                        onReportIncident(type, fullDesc)
                        description = ""
                        localError = null
                        showDialog = false
                    }
                }) { Text(stringResource(R.string.route_map_send), fontWeight = FontWeight.Bold, color = BrandNavy) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ForceMajeureCancelDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val reasonResList = listOf(
        R.string.cancel_reason_weather,
        R.string.cancel_reason_mechanical,
        R.string.cancel_reason_road_closure,
        R.string.cancel_reason_security
    )
    var selectedReasonRes by remember { mutableIntStateOf(reasonResList.first()) }
    var extraDetails by remember { mutableStateOf("") }
    val selectedReasonLabel = stringResource(selectedReasonRes)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.cancel_trip_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Red
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFDAD6),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.cancel_trip_dialog_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF410002),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.cancel_trip_reason_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandNavy
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    reasonResList.forEach { resId ->
                        FilterChip(
                            selected = selectedReasonRes == resId,
                            onClick = { selectedReasonRes = resId },
                            label = { Text(stringResource(resId), style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Red,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                FormField(
                    value = extraDetails,
                    onValueChange = { extraDetails = it },
                    label = stringResource(R.string.cancel_trip_notes_label),
                    singleLine = false
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val reason = if (extraDetails.isBlank()) selectedReasonLabel
                    else "$selectedReasonLabel: ${extraDetails.trim()}"
                    onConfirm(reason)
                }
            ) {
                Text(stringResource(R.string.cancel_trip_confirm_btn), fontWeight = FontWeight.Bold, color = Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
