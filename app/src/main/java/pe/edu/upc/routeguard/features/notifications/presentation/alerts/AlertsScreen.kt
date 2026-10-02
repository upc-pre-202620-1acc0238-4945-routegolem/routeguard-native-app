package pe.edu.upc.routeguard.features.notifications.presentation.alerts

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.FormField
import pe.edu.upc.routeguard.core.designsystem.components.InfoText
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.SecondaryButton
import pe.edu.upc.routeguard.core.designsystem.components.SectionTitle
import pe.edu.upc.routeguard.features.notifications.domain.Notification
import pe.edu.upc.routeguard.features.notifications.domain.NotificationStatus
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
        ScreenHeader(title = "Alertas", onBack = onBack)

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
                        SectionTitle("Aviso a los padres")
                        FormField(
                            value = state.broadcastMessage,
                            onValueChange = viewModel::onBroadcastChange,
                            label = "Mensaje (ej. retraso por tráfico)"
                        )
                        SecondaryButton(
                            text = "Publicar aviso",
                            onClick = viewModel::onSendBroadcast,
                            enabled = state.broadcastMessage.isNotBlank()
                        )
                    }
                }
            }

            item { SectionTitle("Historial") }
            if (state.notifications.isEmpty()) {
                item { Text("Aún no tienes notificaciones.") }
            }
            items(state.notifications, key = { it.id }) { notification ->
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
        MaterialTheme.colorScheme.surfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = notification.title, fontWeight = FontWeight.Bold)
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
        text = "Alerta de pánico",
        onClick = { showDialog = true },
        containerColor = Color(0xFFC62828)
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("¿Enviar alerta de pánico?") },
            text = { Text("Se notificará con alta prioridad a todos los padres de la ruta.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onConfirm()
                }) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

private fun statusLabel(status: NotificationStatus) = when (status) {
    NotificationStatus.PENDING -> "Pendiente"
    NotificationStatus.DISPATCHED -> "Enviada"
    NotificationStatus.DELIVERED -> "Entregada"
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(millis))
