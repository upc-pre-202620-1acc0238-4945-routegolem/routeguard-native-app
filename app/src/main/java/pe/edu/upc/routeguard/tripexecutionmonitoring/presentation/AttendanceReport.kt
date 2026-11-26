package pe.edu.upc.routeguard.tripexecutionmonitoring.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.StatusChip
import pe.edu.upc.routeguard.core.designsystem.theme.BrandBorder
import pe.edu.upc.routeguard.core.designsystem.theme.BrandEmeraldDark
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.BoardingState
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LiveTrip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Trip
import kotlin.math.roundToInt

data class AttendanceRow(
    val studentName: String,
    val status: BoardingState
)

data class AttendanceSummary(
    val total: Int,
    val boarded: Int,
    val absent: Int,
    val pending: Int,
    val attendancePercent: Int,
    val rows: List<AttendanceRow>
)

object AttendanceReportFormatter {

    fun fromTrip(trip: Trip): AttendanceSummary {
        val rows = trip.waypoints.map { AttendanceRow(it.studentName, it.status) }
        return computeSummary(rows)
    }

    fun fromLiveTrip(liveTrip: LiveTrip): AttendanceSummary {
        val rows = if (liveTrip.children.isNotEmpty()) {
            liveTrip.children.map { AttendanceRow(it.name, it.state) }
        } else {
            val total = liveTrip.totalChildren.coerceAtLeast(liveTrip.boardedCount)
            val boarded = liveTrip.boardedCount.coerceIn(0, total)
            (0 until total).map { idx ->
                AttendanceRow(
                    studentName = "Estudiante ${idx + 1}",
                    status = if (idx < boarded) BoardingState.BOARDED else BoardingState.MISSING
                )
            }
        }
        return computeSummary(rows)
    }

    fun computeSummary(rows: List<AttendanceRow>): AttendanceSummary {
        val total = rows.size
        val boarded = rows.count { it.status == BoardingState.BOARDED }
        val absent = rows.count { it.status == BoardingState.OMITTED }
        val pending = rows.count { it.status == BoardingState.MISSING }
        val pct = if (total > 0) ((boarded * 100.0) / total).roundToInt() else 0
        return AttendanceSummary(
            total = total,
            boarded = boarded,
            absent = absent,
            pending = pending,
            attendancePercent = pct,
            rows = rows
        )
    }

    fun toCsv(routeName: String, summary: AttendanceSummary): String {
        val sb = StringBuilder()
        sb.appendLine("Route,Total,Boarded,Absent,Pending,AttendancePercent")
        sb.appendLine("\"${routeName.replace("\"", "\"\"")}\",${summary.total},${summary.boarded},${summary.absent},${summary.pending},${summary.attendancePercent}%")
        sb.appendLine("Student,Status")
        summary.rows.forEach { row ->
            sb.appendLine("\"${row.studentName.replace("\"", "\"\"")}\",${row.status.name}")
        }
        return sb.toString().trimEnd()
    }
}

@Suppress("DEPRECATION")
@Composable
fun AttendanceReportDialog(
    routeName: String,
    summary: AttendanceSummary,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.List, contentDescription = null, tint = BrandNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.attendance_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = routeName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569)
                )

                // Summary metrics grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatBox(stringResource(R.string.attendance_stat_total), summary.total.toString(), BrandNavy, Modifier.weight(1f))
                    StatBox(stringResource(R.string.attendance_stat_boarded), summary.boarded.toString(), BrandEmeraldDark, Modifier.weight(1f))
                    StatBox(stringResource(R.string.attendance_stat_absent), summary.absent.toString(), Color(0xFFBA1A1A), Modifier.weight(1f))
                    StatBox(stringResource(R.string.attendance_stat_pending), summary.pending.toString(), Color(0xFFD97706), Modifier.weight(1f))
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandNavy.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.attendance_stat_rate),
                            style = MaterialTheme.typography.labelMedium,
                            color = BrandNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${summary.attendancePercent}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = BrandNavy,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = BrandBorder)

                Text(
                    text = stringResource(R.string.attendance_students_section),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )

                summary.rows.forEach { row ->
                    val (labelRes, bg, fg, icon) = when (row.status) {
                        BoardingState.BOARDED -> Quad(R.string.route_map_boarding_boarded_chip, Color(0xFFD1FAE5), Color(0xFF064E3B), Icons.Filled.Check)
                        BoardingState.OMITTED -> Quad(R.string.route_map_boarding_omitted, Color(0xFFFFDAD6), Color(0xFFBA1A1A), Icons.Filled.Close)
                        BoardingState.MISSING -> Quad(R.string.route_map_boarding_missing, Color(0xFFF1F5F9), Color(0xFF475569), Icons.Filled.Place)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = row.studentName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        StatusChip(
                            text = stringResource(labelRes),
                            containerColor = bg,
                            contentColor = fg,
                            icon = icon
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        val csv = AttendanceReportFormatter.toCsv(routeName, summary)
                        clipboardManager.setText(AnnotatedString(csv))
                        copied = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (copied) stringResource(R.string.attendance_csv_copied) else stringResource(R.string.attendance_copy_csv),
                        fontWeight = FontWeight.Bold,
                        color = if (copied) BrandEmeraldDark else BrandNavy
                    )
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

@Composable
private fun StatBox(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF475569))
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
