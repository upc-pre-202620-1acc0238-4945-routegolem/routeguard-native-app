package pe.edu.upc.routeguard.tripexecutionmonitoring.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.StatusChip
import pe.edu.upc.routeguard.core.designsystem.theme.BrandBorder
import pe.edu.upc.routeguard.core.designsystem.theme.BrandCardWhite
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripHistoryItem
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripStatus

@Composable
fun TripHistoryDialog(
    trips: List<TripHistoryItem>,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = BrandNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.trip_history_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                when {
                    isLoading && trips.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BrandNavy, modifier = Modifier.size(28.dp))
                        }
                    }

                    trips.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.trip_history_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            trips.forEach { item ->
                                val isCancelled = item.status == TripStatus.CANCELLED
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = BrandCardWhite,
                                    border = BorderStroke(1.dp, BrandBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.routeName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = BrandNavy,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            StatusChip(
                                                text = stringResource(
                                                    if (isCancelled) R.string.trip_history_status_cancelled
                                                    else R.string.trip_history_status_completed
                                                ),
                                                containerColor = if (isCancelled) Color(0xFFFFDAD6) else Color(0xFFD1FAE5),
                                                contentColor = if (isCancelled) Color(0xFFBA1A1A) else Color(0xFF064E3B),
                                                icon = if (isCancelled) Icons.Filled.Close else Icons.Filled.Check
                                            )
                                        }
                                        Text(
                                            text = stringResource(R.string.trip_history_driver_fmt, item.driverName),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF475569)
                                        )
                                        Text(
                                            text = stringResource(
                                                R.string.trip_history_meta,
                                                item.dateLabel,
                                                item.boardedCount,
                                                item.totalStudents,
                                                item.durationMinutes
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF64748B)
                                        )
                                        if (!item.note.isNullOrBlank()) {
                                            Text(
                                                text = item.note,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFBA1A1A)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_done), fontWeight = FontWeight.Bold, color = BrandNavy)
            }
        }
    )
}
