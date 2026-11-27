package pe.edu.upc.routeguard.notificationscommunication.presentation.alerts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.StatusChip
import pe.edu.upc.routeguard.core.designsystem.theme.BrandAmber
import pe.edu.upc.routeguard.core.designsystem.theme.BrandBorder
import pe.edu.upc.routeguard.core.designsystem.theme.BrandCardWhite
import pe.edu.upc.routeguard.core.designsystem.theme.BrandEmeraldDark
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy
import pe.edu.upc.routeguard.core.designsystem.theme.BrandSurfaceBg
import pe.edu.upc.routeguard.core.designsystem.theme.BrandTeal
import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationStatus
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PanicRed = Color(0xFFB91C1C)
private val PanicCardBg = Color(0xFFFFECEB)
private val PanicCardBorder = Color(0xFFFCA5A5)
private val PanicTitleInk = Color(0xFF7F1D1D)

/** Notifications for parents; the driver also gets the panic button and announcements card. */
@Composable
fun AlertsScreen(
    modifier: Modifier = Modifier,
    isDriver: Boolean = false,
    viewModel: NotificationsViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) { viewModel.refresh(confirmDelivery = !isDriver) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandSurfaceBg)
    ) {
        AlertsTopHeader(
            title = stringResource(R.string.alerts_title),
            isRefreshing = state.isLoading && state.notifications.isNotEmpty(),
            onRefresh = { viewModel.refresh(confirmDelivery = !isDriver) },
            onBack = onBack
        )

        if (state.isLoading && state.notifications.isEmpty()) {
            LoadingBox()
            return@Column
        }

        val visibleNotifications = state.filteredNotifications

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!state.message.isNullOrBlank()) {
                item { SuccessFeedbackBanner(state.message) }
            }

            if (!state.errorMessage.isNullOrBlank()) {
                item {
                    ErrorText(
                        message = state.errorMessage,
                        onRetry = { viewModel.refresh(confirmDelivery = !isDriver) }
                    )
                }
            }

            if (isDriver) {
                item {
                    PanicButton(
                        enabled = !state.isSubmitting,
                        onConfirm = viewModel::onPanic
                    )
                }
                item {
                    DriverBroadcastCard(
                        message = state.broadcastMessage,
                        isSubmitting = state.isSubmitting,
                        onMessageChange = viewModel::onBroadcastChange,
                        onSend = viewModel::onSendBroadcast
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (isDriver) 4.dp else 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            if (isDriver) R.string.alerts_history_driver
                            else R.string.alerts_history_parent
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandNavy.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = stringResource(R.string.alerts_count_badge, visibleNotifications.size),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandNavy,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            item {
                AlertFilterBar(
                    selected = state.selectedFilter,
                    onSelect = viewModel::onFilterSelected
                )
            }

            if (visibleNotifications.isEmpty()) {
                item {
                    EmptyAlertsCard(isFiltered = state.notifications.isNotEmpty())
                }
            } else {
                items(visibleNotifications, key = { it.id.value }) { notification ->
                    NotificationCard(notification)
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun AlertsTopHeader(
    title: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onBack: (() -> Unit)?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BrandCardWhite,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, BrandBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = BrandNavy
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BrandNavy,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = BrandNavy
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.alerts_refresh_cd),
                        tint = BrandNavy
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverBroadcastCard(
    message: String,
    isSubmitting: Boolean,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandNavy.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = BrandNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.alerts_broadcast_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
            }

            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                minLines = 2,
                maxLines = 3,
                placeholder = { Text(stringResource(R.string.alerts_broadcast_hint)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BrandSurfaceBg,
                    unfocusedContainerColor = BrandSurfaceBg,
                    focusedBorderColor = BrandNavy,
                    unfocusedBorderColor = BrandBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = onSend,
                enabled = !isSubmitting && message.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 46.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.5.dp,
                    if (!isSubmitting && message.isNotBlank()) BrandNavy else BrandBorder
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BrandNavy
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = BrandNavy
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.alerts_broadcast_send),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AlertFilterBar(
    selected: AlertFilter,
    onSelect: (AlertFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val options = listOf(
            AlertFilter.ALL to R.string.alerts_filter_all,
            AlertFilter.PRIORITY to R.string.alerts_filter_priority,
            AlertFilter.TRIP to R.string.alerts_filter_trip,
            AlertFilter.NOTICES to R.string.alerts_filter_notices
        )
        options.forEach { (filter, labelRes) ->
            val isSelected = selected == filter
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(filter) },
                label = {
                    Text(
                        text = stringResource(labelRes),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrandNavy,
                    selectedLabelColor = Color.White,
                    containerColor = BrandCardWhite,
                    labelColor = Color(0xFF475569)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = BrandBorder,
                    selectedBorderColor = BrandNavy
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun SuccessFeedbackBanner(message: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFD1FAE5),
        border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = BrandEmeraldDark,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF064E3B)
            )
        }
    }
}

@Composable
private fun EmptyAlertsCard(isFiltered: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, BrandBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(BrandNavy.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = BrandNavy,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = stringResource(R.string.alerts_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrandNavy,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(
                    if (isFiltered) R.string.alerts_empty_filtered
                    else R.string.alerts_empty
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class AlertVisualStyle(
    val icon: ImageVector,
    val accent: Color,
    val categoryLabelRes: Int
)

@Composable
private fun NotificationCard(notification: Notification) {
    val isSchoolArrival = notification.type == NotificationType.GEOFENCE_BREACHED && (
        notification.title.equals("Llegada al colegio", ignoreCase = true) ||
            notification.body.contains("Llegada al colegio", ignoreCase = true) ||
            notification.body.contains("School arrival", ignoreCase = true)
        )

    val visual = when (notification.type) {
        NotificationType.PANIC_ALERT -> AlertVisualStyle(
            icon = Icons.Filled.Warning,
            accent = PanicRed,
            categoryLabelRes = R.string.alerts_cat_panic
        )
        NotificationType.INCIDENT_REPORTED -> AlertVisualStyle(
            icon = Icons.Filled.Warning,
            accent = BrandAmber,
            categoryLabelRes = R.string.alerts_cat_incident
        )
        NotificationType.STUDENT_BOARDED -> AlertVisualStyle(
            icon = Icons.Filled.CheckCircle,
            accent = BrandEmeraldDark,
            categoryLabelRes = R.string.alerts_cat_boarding
        )
        NotificationType.GEOFENCE_BREACHED -> if (isSchoolArrival) {
            AlertVisualStyle(
                icon = Icons.Filled.Place,
                accent = BrandNavy,
                categoryLabelRes = R.string.alerts_cat_arrival
            )
        } else {
            AlertVisualStyle(
                icon = Icons.Filled.LocationOn,
                accent = BrandTeal,
                categoryLabelRes = R.string.alerts_cat_geofence
            )
        }
        NotificationType.TRIP_STARTED -> AlertVisualStyle(
            icon = Icons.Filled.Info,
            accent = BrandTeal,
            categoryLabelRes = R.string.alerts_cat_trip
        )
        NotificationType.ANNOUNCEMENT, NotificationType.GENERAL -> AlertVisualStyle(
            icon = Icons.Filled.Notifications,
            accent = BrandNavy,
            categoryLabelRes = R.string.alerts_cat_notice
        )
    }

    val isPanic = notification.isHighPriority
    val containerColor = if (isPanic) PanicCardBg else BrandCardWhite
    val borderColor = if (isPanic) PanicCardBorder else BrandBorder
    val titleColor = if (isPanic) PanicTitleInk else Color(0xFF0F172A)
    val bodyColor = if (isPanic) PanicTitleInk else Color(0xFF334155)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPanic) 3.dp else 1.5.dp),
        border = BorderStroke(if (isPanic) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(visual.accent.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = null,
                    tint = visual.accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = localizedNotificationTitle(notification, isSchoolArrival),
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = formatDate(notification.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                }

                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    ),
                    color = bodyColor
                )

                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(
                        text = stringResource(visual.categoryLabelRes),
                        containerColor = visual.accent.copy(alpha = 0.12f),
                        contentColor = visual.accent
                    )
                    StatusChip(
                        text = statusLabel(notification.status),
                        containerColor = if (notification.isDelivered) {
                            Color(0xFFD1FAE5)
                        } else {
                            Color(0xFFF1F5F9)
                        },
                        contentColor = if (notification.isDelivered) {
                            Color(0xFF064E3B)
                        } else {
                            Color(0xFF475569)
                        },
                        icon = if (notification.isDelivered) Icons.Filled.Check else null
                    )
                }
            }
        }
    }
}

@Composable
private fun PanicButton(enabled: Boolean, onConfirm: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    BigButton(
        text = stringResource(R.string.alerts_panic_fab),
        onClick = { showDialog = true },
        enabled = enabled,
        containerColor = PanicRed,
        icon = Icons.Filled.Warning
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = PanicRed,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = stringResource(R.string.alerts_panic_dialog_title),
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }
            },
            text = {
                Text(
                    text = stringResource(R.string.alerts_panic_dialog_body),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onConfirm()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PanicRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.alerts_panic_confirm),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun localizedNotificationTitle(
    notification: Notification,
    isSchoolArrival: Boolean
): String = when (notification.type) {
    NotificationType.ANNOUNCEMENT -> stringResource(R.string.notif_title_announcement)
    NotificationType.TRIP_STARTED -> stringResource(R.string.notif_title_trip_started)
    NotificationType.PANIC_ALERT -> stringResource(R.string.notif_title_panic)
    NotificationType.GEOFENCE_BREACHED -> {
        if (isSchoolArrival) stringResource(R.string.geofence_arrived_school)
        else stringResource(R.string.notif_title_geofence)
    }
    NotificationType.STUDENT_BOARDED -> stringResource(R.string.notif_title_student_boarded)
    NotificationType.INCIDENT_REPORTED -> stringResource(R.string.notif_title_incident)
    NotificationType.GENERAL -> notification.title.ifBlank { stringResource(R.string.notif_title_general) }
}

@Composable
private fun statusLabel(status: NotificationStatus) = when (status) {
    NotificationStatus.PENDING -> stringResource(R.string.notif_status_pending)
    NotificationStatus.DISPATCHED -> stringResource(R.string.notif_status_sent)
    NotificationStatus.DELIVERED -> stringResource(R.string.notif_status_delivered)
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd/MM · HH:mm", Locale.getDefault()).format(Date(millis))
