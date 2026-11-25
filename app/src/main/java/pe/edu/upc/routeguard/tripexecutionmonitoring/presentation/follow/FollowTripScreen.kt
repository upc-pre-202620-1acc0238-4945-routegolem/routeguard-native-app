package pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.follow

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.SecondaryButton
import pe.edu.upc.routeguard.core.designsystem.components.StatusChip
import pe.edu.upc.routeguard.core.designsystem.components.map.MapStop
import pe.edu.upc.routeguard.core.designsystem.components.map.MapVehicle
import pe.edu.upc.routeguard.core.designsystem.components.map.RouteGuardMap
import pe.edu.upc.routeguard.core.designsystem.theme.BrandBorder
import pe.edu.upc.routeguard.core.designsystem.theme.BrandCardWhite
import pe.edu.upc.routeguard.core.designsystem.theme.BrandEmerald
import pe.edu.upc.routeguard.core.designsystem.theme.BrandEmeraldDark
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.withEstimate
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.localizedFormatAge
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.localizedParentLabel
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.localizedProgressText

private const val REFRESH_MS = 5_000L
private const val VEHICLE_ID = "vehicle"
private const val STALE_THRESHOLD_MS = 180_000L // 3 minutes

/** Parent view: where is the vehicle that carries my children, speed, and real-time stops. */
@Composable
fun FollowTripScreen(
    modifier: Modifier = Modifier,
    viewModel: FollowTripViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    // Polls while the screen is visible.
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refresh()
            delay(REFRESH_MS)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.follow_title))

        val trip = state.trip
        when {
            !state.hasLoaded -> LoadingBox()

            trip == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ErrorText(state.errorMessage)
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
                            text = stringResource(R.string.follow_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy
                        )
                        Text(
                            text = stringResource(R.string.follow_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            else -> Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val nextStop = trip.nextStop?.withEstimate(state.arrival)

                // Interactive Map View
                RouteGuardMap(
                    stops = trip.stops.map {
                        MapStop(it.name, it.coordinates, it.order, it.reached, it.order == nextStop?.order)
                    },
                    vehicles = trip.location?.let {
                        listOf(MapVehicle(VEHICLE_ID, stringResource(R.string.follow_map_vehicle_label), it.coordinates))
                    }.orEmpty(),
                    followVehicleId = VEHICLE_ID,
                    routePath = state.roadPath,
                    height = 320.dp,
                    fitKey = trip.tripId
                )

                ErrorText(state.errorMessage)

                // Live Vehicle Telemetry Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            StatusChip(
                                text = stringResource(R.string.follow_badge_en_route),
                                containerColor = Color(0xFFD1FAE5),
                                contentColor = Color(0xFF064E3B),
                                icon = Icons.Filled.Check
                            )
                        }

                        val location = trip.location
                        if (location != null) {
                            val isStale = (System.currentTimeMillis() - location.recordedAt) > STALE_THRESHOLD_MS
                            val ageLabel = localizedFormatAge(location.recordedAt)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatusChip(
                                    text = "${location.speedKmh.toInt()} km/h",
                                    containerColor = BrandNavy.copy(alpha = 0.08f),
                                    contentColor = BrandNavy
                                )
                                StatusChip(
                                    text = stringResource(R.string.follow_updated_ago, ageLabel),
                                    containerColor = Color(0xFFF1F5F9),
                                    contentColor = Color(0xFF475569)
                                )
                            }

                            if (isStale) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            stringResource(R.string.follow_gps_stale_warning, ageLabel),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                stringResource(R.string.follow_waiting_first_gps),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                // Ficha de seguridad y trazabilidad del vehículo (Carmen V-01)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.follow_vehicle_card_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Text(
                                    text = stringResource(R.string.follow_vehicle_plate_chip),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.follow_vehicle_unit_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = stringResource(
                                R.string.follow_vehicle_driver_dni,
                                trip.driverName.ifBlank { stringResource(R.string.follow_driver_authorized_fallback) }
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD1FAE5)
                        ) {
                            Text(
                                text = stringResource(R.string.follow_vehicle_soat_status),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF064E3B),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Next Stop Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.follow_next_stop_label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy
                        )
                        Text(
                            text = if (nextStop == null) {
                                stringResource(R.string.follow_next_stop_all_passed)
                            } else {
                                "${nextStop.name} · ${nextStop.localizedProgressText()}"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF334155)
                        )
                    }
                }

                // Children in Route Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.follow_children_card_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy
                        )
                        if (trip.children.isEmpty()) {
                            Text(
                                text = stringResource(R.string.follow_children_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        } else {
                            trip.children.forEach { child ->
                                val childStatusText = child.state.localizedParentLabel()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(BrandNavy.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = child.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = BrandNavy
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = child.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = stringResource(R.string.follow_child_state_prefix, childStatusText),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    StatusChip(
                                        text = childStatusText,
                                        containerColor = Color(0xFFD1FAE5),
                                        contentColor = Color(0xFF064E3B)
                                    )
                                }
                            }
                        }
                    }
                }

                // SOS / Llamada directa al conductor (RuwaLabs #12, Miriam V-03)
                BigButton(
                    text = stringResource(
                        R.string.follow_call_driver_btn,
                        trip.driverName.ifBlank { stringResource(R.string.follow_driver_staff_fallback) }
                    ),
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+51987654321"))
                        context.startActivity(intent)
                    },
                    containerColor = BrandEmeraldDark,
                    icon = Icons.Filled.Call
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
