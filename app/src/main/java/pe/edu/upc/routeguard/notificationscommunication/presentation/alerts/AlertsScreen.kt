package pe.edu.upc.routeguard.notificationscommunication.presentation.alerts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.FormField
import pe.edu.upc.routeguard.core.designsystem.components.InfoText
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.SecondaryButton
import pe.edu.upc.routeguard.core.designsystem.components.SectionTitle
import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationStatus
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Notifications for parents; the driver also gets the panic button and announcements. */
@Composable
fun AlertsScreen(
    modifier: Modifier = Modifier,
    isDriver: Boolean = false,
    viewModel: NotificationsViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) { viewModel.refresh(confirmDelivery = !isDriver) }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.alerts_title), onBack = onBack)

        if (state.isLoading && state.notifications.isEmpty()) {
            LoadingBox()
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                InfoText(state.message)
                ErrorText(state.errorMessage)
            }

            if (isDriver) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PanicButton(onConfirm = viewModel::onPanic)
                        SectionTitle(stringResource(R.string.alerts_broadcast_section))
                        FormField(
                            value = state.broadcastMessage,
                            onValueChange = viewModel::onBroadcastChange,
                            label = stringResource(R.string.alerts_broadcast_label)
                        )
                        SecondaryButton(
                            text = stringResource(R.string.alerts_broadcast_send),
                            onClick = viewModel::onSendBroadcast,
                            enabled = state.broadcastMessage.isNotBlank()
                        )
                    }
                }
            }

            item { SectionTitle(stringResource(R.string.alerts_history_title)) }
            if (state.notifications.isEmpty()) {
                item { Text(stringResource(R.string.alerts_empty)) }
            }
            items(state.notifications, key = { it.id.value }) { notification ->
                NotificationCard(notification)
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: Notification) {
    val containerColor = if (notification.isHighPriority) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            1.dp,
            if (notification.isHighPriority) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = localizedNotificationTitle(notification), fontWeight = FontWeight.Bold)
            Text(text = notification.body)
            Text(
                text = formatDate(notification.createdAt) + " · " + statusLabel(notification.status),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PanicButton(onConfirm: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    BigButton(
        text = stringResource(R.string.alerts_panic_fab),
        onClick = { showDialog = true },
        containerColor = Color(0xFFB91C1C)
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.alerts_panic_dialog_title)) },
            text = { Text(stringResource(R.string.alerts_panic_dialog_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onConfirm()
                }) { Text(stringResource(R.string.alerts_panic_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }
}

@Composable
private fun localizedNotificationTitle(notification: Notification): String = when (notification.type) {
    NotificationType.ANNOUNCEMENT -> stringResource(R.string.notif_title_announcement)
    NotificationType.TRIP_STARTED -> stringResource(R.string.notif_title_trip_started)
    NotificationType.PANIC_ALERT -> stringResource(R.string.notif_title_panic)
    NotificationType.GEOFENCE_BREACHED -> {
        if (notification.title == "Llegada al colegio") stringResource(R.string.geofence_arrived_school)
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
    SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(millis))
