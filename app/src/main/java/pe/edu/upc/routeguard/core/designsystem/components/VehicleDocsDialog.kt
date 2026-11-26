package pe.edu.upc.routeguard.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import pe.edu.upc.routeguard.core.designsystem.theme.BrandBorder
import pe.edu.upc.routeguard.core.designsystem.theme.BrandEmeraldDark
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy

/** Official SOAT and Driver License documentation metadata (US-12). */
data class VehicleDocumentation(
    val plate: String = "ABC-123",
    val unitModel: String = "Mercedes-Benz Sprinter 416",
    val soatPolicy: String = "SOAT-2026-88412",
    val soatInsurer: String = "Rímac Seguros",
    val soatExpiry: String = "2026-12-15",
    val driverName: String = "Carlos Ramirez",
    val driverDni: String = "08492019",
    val licenseNumber: String = "A-IIIa",
    val licenseExpiry: String = "2027-08-30"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VehicleDocsStatusBadges(
    licenseCode: String = "A-IIIa",
    soatPolicy: String = "SOAT-2026-88412",
    soatExpiry: String = "15/12/2026",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusChip(
                text = stringResource(R.string.docs_soat_badge_valid),
                containerColor = Color(0xFFD1FAE5),
                contentColor = Color(0xFF064E3B),
                icon = Icons.Filled.Check
            )
            StatusChip(
                text = stringResource(R.string.docs_license_badge, licenseCode),
                containerColor = BrandNavy.copy(alpha = 0.08f),
                contentColor = BrandNavy,
                icon = Icons.Filled.Person
            )
        }
        Text(
            text = stringResource(R.string.docs_soat_policy_summary, soatPolicy, soatExpiry),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF475569)
        )
    }
}

@Composable
fun VehicleDocsDialog(
    docs: VehicleDocumentation,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = BrandEmeraldDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.docs_dialog_title),
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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.docs_vehicle_section),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
                DocDetailRow(stringResource(R.string.docs_vehicle_plate_label), docs.plate)
                DocDetailRow(stringResource(R.string.docs_soat_policy_label), docs.soatPolicy)
                DocDetailRow(stringResource(R.string.docs_soat_insurer_label), docs.soatInsurer)
                DocDetailRow(
                    stringResource(R.string.docs_soat_expiry_label),
                    "${docs.soatExpiry} · ${stringResource(R.string.docs_status_valid)}"
                )
                DocDetailRow(stringResource(R.string.docs_tech_review_label), stringResource(R.string.docs_tech_review_val))

                HorizontalDivider(color = BrandBorder)

                Text(
                    text = stringResource(R.string.docs_driver_section),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
                DocDetailRow(stringResource(R.string.docs_driver_name_label), "${docs.driverName} (DNI ${docs.driverDni})")
                DocDetailRow(stringResource(R.string.docs_driver_license_label), docs.licenseNumber)
                DocDetailRow(
                    stringResource(R.string.docs_driver_license_expiry_label),
                    "${docs.licenseExpiry} · ${stringResource(R.string.docs_status_valid)}"
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFD1FAE5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF064E3B), modifier = Modifier.size(18.dp))
                        Text(
                            text = stringResource(R.string.docs_verified_note),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF064E3B),
                            fontWeight = FontWeight.SemiBold
                        )
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

@Composable
private fun DocDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A)
        )
    }
}
