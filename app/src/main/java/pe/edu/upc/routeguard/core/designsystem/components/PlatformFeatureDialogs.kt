package pe.edu.upc.routeguard.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.database.ChatMessageEntity
import pe.edu.upc.routeguard.core.database.OperationalExpenseEntity
import pe.edu.upc.routeguard.core.database.PickupChangeEntity
import pe.edu.upc.routeguard.core.database.RouteAnnouncementEntity
import pe.edu.upc.routeguard.core.database.VehicleMaintenanceEntity
import pe.edu.upc.routeguard.core.designsystem.theme.BrandBorder
import pe.edu.upc.routeguard.core.designsystem.theme.BrandEmerald
import pe.edu.upc.routeguard.core.designsystem.theme.BrandEmeraldDark
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy
import pe.edu.upc.routeguard.core.network.OptimizedRouteDto

/** US-24: Driver & Admin Announcements Board Dialog. */
@Composable
fun RouteAnnouncementsDialog(
    announcements: List<RouteAnnouncementEntity>,
    canPublish: Boolean,
    onPublish: (title: String, message: String, priority: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("NORMAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(stringResource(R.string.announcements_title), fontWeight = FontWeight.Bold, color = BrandNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (canPublish) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.announcements_field_title)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text(stringResource(R.string.announcements_field_message)) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = priority == "NORMAL",
                            onClick = { priority = "NORMAL" },
                            label = { Text(stringResource(R.string.priority_normal)) }
                        )
                        FilterChip(
                            selected = priority == "IMPORTANT",
                            onClick = { priority = "IMPORTANT" },
                            label = { Text(stringResource(R.string.priority_important)) }
                        )
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank() && message.isNotBlank()) {
                                onPublish(title, message, priority)
                                title = ""
                                message = ""
                            }
                        },
                        enabled = title.isNotBlank() && message.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.announcements_btn_publish))
                    }
                    HorizontalDivider()
                }
                if (announcements.isEmpty()) {
                    Text(stringResource(R.string.announcements_empty), style = MaterialTheme.typography.bodySmall)
                } else {
                    announcements.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandBorder),
                            color = if (item.priority == "IMPORTANT") Color(0xFFFEF3C7) else Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(item.title, fontWeight = FontWeight.Bold, color = BrandNavy)
                                Text(item.message, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${item.authorName} · ${item.createdAt.take(10)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_close)) }
        }
    )
}

/** US-30: Internal Route Chat Dialog (Parent <-> Driver <-> Admin). */
@Composable
fun RouteChatDialog(
    messages: List<ChatMessageEntity>,
    currentUserId: String,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(stringResource(R.string.chat_title), fontWeight = FontWeight.Bold, color = BrandNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (messages.isEmpty()) {
                        Text(stringResource(R.string.chat_empty), style = MaterialTheme.typography.bodySmall)
                    } else {
                        messages.forEach { msg ->
                            val isMine = msg.senderUserId == currentUserId && currentUserId.isNotBlank()
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isMine) BrandNavy.copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        "${msg.senderName} (${msg.senderRole})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandNavy
                                    )
                                    Text(msg.message, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text(stringResource(R.string.chat_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (draft.isNotBlank()) {
                            onSend(draft)
                            draft = ""
                        }
                    },
                    enabled = draft.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.chat_send_btn))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_close)) }
        }
    )
}

/** US-20: Vehicle Maintenance History Dialog. */
@Composable
fun MaintenanceHistoryDialog(
    maintenances: List<VehicleMaintenanceEntity>,
    canAdd: Boolean,
    defaultPlate: String,
    onAdd: (plate: String, type: String, description: String, workshop: String, cost: Double, odometerKm: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var plate by remember(defaultPlate) { mutableStateOf(defaultPlate.ifBlank { "ABC-123" }) }
    var type by remember { mutableStateOf("") }
    var workshop by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(stringResource(R.string.maintenances_title), fontWeight = FontWeight.Bold, color = BrandNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (canAdd) {
                    OutlinedTextField(
                        value = plate,
                        onValueChange = { plate = it },
                        label = { Text(stringResource(R.string.field_vehicle_plate)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = { Text(stringResource(R.string.maintenances_field_type)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = workshop,
                            onValueChange = { workshop = it },
                            label = { Text(stringResource(R.string.maintenances_field_workshop)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = costText,
                            onValueChange = { costText = it },
                            label = { Text(stringResource(R.string.field_cost_pen)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button(
                        onClick = {
                            val cost = costText.toDoubleOrNull() ?: 0.0
                            if (type.isNotBlank() && cost >= 0) {
                                onAdd(plate, type, type, workshop, cost, 45000)
                                type = ""
                                workshop = ""
                                costText = ""
                            }
                        },
                        enabled = type.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.maintenances_btn_add))
                    }
                    HorizontalDivider()
                }
                if (maintenances.isEmpty()) {
                    Text(stringResource(R.string.maintenances_empty), style = MaterialTheme.typography.bodySmall)
                } else {
                    maintenances.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("${item.vehiclePlate} · ${item.maintenanceType}", fontWeight = FontWeight.Bold, color = BrandNavy)
                                if (item.description.isNotBlank()) {
                                    Text(item.description, style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    "${item.workshop} · S/ ${String.format("%.2f", item.cost)} · ${item.performedAt}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandEmeraldDark
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_close)) }
        }
    )
}

/** US-26: Operational Fleet Expenses Dialog. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OperationalExpensesDialog(
    expenses: List<OperationalExpenseEntity>,
    defaultPlate: String,
    onAdd: (plate: String, category: String, description: String, amount: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var plate by remember(defaultPlate) { mutableStateOf(defaultPlate.ifBlank { "ABC-123" }) }
    var category by remember { mutableStateOf("FUEL") }
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(stringResource(R.string.expenses_title), fontWeight = FontWeight.Bold, color = BrandNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "FUEL" to R.string.expense_cat_fuel,
                        "TOLL" to R.string.expense_cat_toll,
                        "PARKING" to R.string.expense_cat_parking
                    ).forEach { (key, labelRes) ->
                        FilterChip(
                            selected = category == key,
                            onClick = { category = key },
                            label = { Text(stringResource(labelRes)) }
                        )
                    }
                }
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text(stringResource(R.string.field_vehicle_plate)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.field_description)) },
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text(stringResource(R.string.field_cost_pen)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onAdd(plate, category, description, amount)
                            description = ""
                            amountText = ""
                        }
                    },
                    enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.expenses_btn_add))
                }
                HorizontalDivider()
                if (expenses.isEmpty()) {
                    Text(stringResource(R.string.expenses_empty), style = MaterialTheme.typography.bodySmall)
                } else {
                    expenses.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${item.category} · ${item.vehiclePlate}", fontWeight = FontWeight.Bold, color = BrandNavy)
                                    if (item.description.isNotBlank()) {
                                        Text(item.description, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(item.expenseDate, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                                }
                                Text(
                                    "S/ ${String.format("%.2f", item.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    color = BrandEmeraldDark
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_close)) }
        }
    )
}

/** US-27 & US-28: Temporary Pickup Address Changes & Absence Stop Optimization Dialog. */
@Composable
fun PickupAndOptimizationDialog(
    canRequestChange: Boolean,
    canApproveChanges: Boolean,
    defaultChildName: String,
    pickupChanges: List<PickupChangeEntity>,
    optimizedRoute: OptimizedRouteDto?,
    onRequestChange: (childName: String, targetDate: String, newAddress: String, reason: String) -> Unit,
    onReviewChange: (requestId: String, status: String) -> Unit,
    onDismiss: () -> Unit
) {
    var childName by remember(defaultChildName) { mutableStateOf(defaultChildName) }
    var newAddress by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(stringResource(R.string.pickup_opt_title), fontWeight = FontWeight.Bold, color = BrandNavy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (canRequestChange) {
                    Text(stringResource(R.string.pickup_change_section), fontWeight = FontWeight.SemiBold, color = BrandNavy)
                    OutlinedTextField(
                        value = childName,
                        onValueChange = { childName = it },
                        label = { Text(stringResource(R.string.pickup_field_child)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text(stringResource(R.string.pickup_field_address)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text(stringResource(R.string.pickup_field_reason)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (childName.isNotBlank() && newAddress.isNotBlank()) {
                                onRequestChange(childName, "", newAddress, reason)
                                newAddress = ""
                                reason = ""
                            }
                        },
                        enabled = childName.isNotBlank() && newAddress.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.pickup_btn_submit))
                    }
                    HorizontalDivider()
                }

                if (optimizedRoute != null) {
                    Text(
                        stringResource(
                            R.string.opt_stops_summary,
                            optimizedRoute.activeStopsCount,
                            optimizedRoute.totalOriginalStops,
                            optimizedRoute.absentStudents.orEmpty().size
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = BrandEmeraldDark
                    )
                    optimizedRoute.stops.orEmpty().forEach { stop ->
                        val skipped = stop.status == "SKIPPED_ABSENT"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (skipped) Color(0xFFFEE2E2) else Color(0xFFF0FDF4),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "#${stop.reassignedOrder ?: "-"} · ${stop.name}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (skipped) Color(0xFF991B1B) else BrandNavy
                                )
                                Text(
                                    stop.status + (stop.absentChildName?.let { " ($it)" } ?: ""),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }

                if (pickupChanges.isNotEmpty()) {
                    Text(stringResource(R.string.pickup_requests_list_title), fontWeight = FontWeight.SemiBold, color = BrandNavy)
                    pickupChanges.forEach { req ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${req.childName} · ${req.status}", fontWeight = FontWeight.Bold, color = BrandNavy)
                                Text(req.newAddress, style = MaterialTheme.typography.bodySmall)
                                if (req.reason.isNotBlank()) {
                                    Text(req.reason, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                                }
                                if (canApproveChanges && req.status == "PENDING" && !req.id.startsWith("local_")) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { onReviewChange(req.id, "APPROVED") }) {
                                            Text(stringResource(R.string.btn_approve))
                                        }
                                        OutlinedButton(onClick = { onReviewChange(req.id, "REJECTED") }) {
                                            Text(stringResource(R.string.btn_reject))
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
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_close)) }
        }
    )
}
