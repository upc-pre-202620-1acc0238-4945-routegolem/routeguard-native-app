package pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.monitor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
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
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.withEstimate
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.AttendanceReportDialog
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.AttendanceReportFormatter
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.localizedFormatAge
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.localizedProgressText

private const val REFRESH_MS = 5_000L

/** Administrator monitor: every trip in progress on a single map with capacity telemetry and legend. */
@Composable
fun LiveMonitorScreen(
    modifier: Modifier = Modifier,
    viewModel: LiveMonitorViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    var showAttendanceReport by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refresh()
            delay(REFRESH_MS)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.monitor_title))

        if (!state.hasLoaded) {
            LoadingBox()
            return@Column
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ErrorText(state.errorMessage)

            val selected = state.selectedTrip
            if (selected == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BrandNavy.copy(alpha = 0.1f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = BrandNavy, modifier = Modifier.size(28.dp))
                            }
                        }
                        Text(
                            text = stringResource(R.string.monitor_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy
                        )
                        Text(
                            text = stringResource(R.string.monitor_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                return@Column
            }

            // Interactive Multi-Vehicle Map
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

            // Map Legend Card (Paridad con Flutter)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BrandCardWhite,
                border = BorderStroke(1.dp, BrandBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFBA1A1A)))
                        Text(stringResource(R.string.monitor_legend_selected), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF047857)))
                        Text(stringResource(R.string.monitor_legend_moving), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF64748B)))
                        Text(stringResource(R.string.monitor_legend_stopped), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            SectionTitle(text = stringResource(R.string.monitor_active_section, state.trips.size))

            // Active Vehicle Cards with Capacity Progress
            state.trips.forEach { trip ->
                val isSelected = trip.tripId == selected.tripId
                val next = trip.nextStop?.let {
                    if (isSelected) it.withEstimate(state.arrival) else it
                }
                val capacityRatio = if (trip.totalChildren > 0) {
                    (trip.boardedCount.toFloat() / trip.totalChildren.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onTripSelected(trip.tripId.value) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) BrandNavy else BrandBorder
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = trip.routeName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy
                                )
                                Text(
                                    text = stringResource(
                                        R.string.monitor_driver_chip,
                                        trip.driverName.ifBlank { stringResource(R.string.monitor_driver_fallback) }
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                            if (isSelected) {
                                StatusChip(
                                    text = stringResource(R.string.monitor_badge_focused),
                                    containerColor = BrandNavy,
                                    contentColor = Color.White
                                )
                            } else {
                                StatusChip(
                                    text = stringResource(R.string.monitor_badge_active),
                                    containerColor = Color(0xFFD1FAE5),
                                    contentColor = Color(0xFF064E3B)
                                )
                            }
                        }

                        // Capacity Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.monitor_occupancy_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = stringResource(
                                        R.string.monitor_occupancy_progress,
                                        trip.boardedCount,
                                        trip.totalChildren,
                                        (capacityRatio * 100).toInt()
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy
                                )
                            }
                            LinearProgressIndicator(
                                progress = { capacityRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (capacityRatio >= 0.8f) Color(0xFFD97706) else BrandNavy,
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }

                        // Telemetry Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusChip(
                                text = trip.location?.let { "${it.speedKmh.toInt()} km/h" } ?: "0 km/h",
                                containerColor = BrandNavy.copy(alpha = 0.08f),
                                contentColor = BrandNavy
                            )
                            StatusChip(
                                text = trip.location?.let { localizedFormatAge(it.recordedAt) } ?: stringResource(R.string.monitor_no_signal),
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF475569)
                            )
                        }

                        Text(
                            text = next?.let { stringResource(R.string.monitor_next_stop_format, it.name, it.localizedProgressText()) }
                                ?: stringResource(R.string.monitor_trip_completed),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            // US-17: Attendance Report button for selected fleet unit
            SecondaryButton(
                text = stringResource(R.string.attendance_report_btn),
                onClick = { showAttendanceReport = true },
                icon = Icons.Filled.List
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    val selectedTrip = state.selectedTrip
    if (showAttendanceReport && selectedTrip != null) {
        AttendanceReportDialog(
            routeName = selectedTrip.routeName,
            summary = AttendanceReportFormatter.fromLiveTrip(selectedTrip),
            onDismiss = { showAttendanceReport = false }
        )
    }
}
