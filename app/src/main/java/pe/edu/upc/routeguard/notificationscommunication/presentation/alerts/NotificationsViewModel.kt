package pe.edu.upc.routeguard.notificationscommunication.presentation.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.localization.AppStringsProvider
import pe.edu.upc.routeguard.notificationscommunication.application.GetNotificationsUseCase
import pe.edu.upc.routeguard.notificationscommunication.application.MarkNotificationsDeliveredUseCase
import pe.edu.upc.routeguard.notificationscommunication.application.PostBroadcastMessageUseCase
import pe.edu.upc.routeguard.notificationscommunication.application.TriggerPanicAlertUseCase
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationStatus
import pe.edu.upc.routeguard.tripexecutionmonitoring.application.GetActiveTripUseCase
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotifications: GetNotificationsUseCase,
    private val markDelivered: MarkNotificationsDeliveredUseCase,
    private val triggerPanicAlert: TriggerPanicAlertUseCase,
    private val postBroadcastMessage: PostBroadcastMessageUseCase,
    // Customer/supplier relation with Trip: the driver communicates about the trip in progress.
    private val getActiveTrip: GetActiveTripUseCase,
    private val strings: AppStringsProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    /** Parents confirm reception of what they see ([confirmDelivery]). */
    fun refresh(confirmDelivery: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val list = getNotifications()
                _uiState.update { it.copy(isLoading = false, notifications = list) }
                if (confirmDelivery) {
                    markDelivered(list)
                    _uiState.update { state ->
                        state.copy(notifications = state.notifications.map { it.copy(status = NotificationStatus.DELIVERED) })
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = strings.resolveError(e, R.string.error_unexpected)) }
            }
        }
    }

    fun onBroadcastChange(value: String) = _uiState.update { it.copy(broadcastMessage = value) }

    fun onPanic() {
        viewModelScope.launch {
            triggerPanicAlert(getActiveTrip()?.id?.value)
                .onSuccess { count ->
                    _uiState.update {
                        it.copy(message = strings.getString(R.string.notif_sent_panic_count, count), errorMessage = null)
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(errorMessage = strings.resolveError(e, R.string.notif_error_panic)) } }
        }
    }

    fun onSendBroadcast() {
        viewModelScope.launch {
            postBroadcastMessage(getActiveTrip()?.id?.value, _uiState.value.broadcastMessage)
                .onSuccess { count ->
                    _uiState.update {
                        it.copy(broadcastMessage = "", message = strings.getString(R.string.notif_sent_broadcast_count, count), errorMessage = null)
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(errorMessage = strings.resolveError(e, R.string.notif_error_panic)) } }
        }
    }
}
