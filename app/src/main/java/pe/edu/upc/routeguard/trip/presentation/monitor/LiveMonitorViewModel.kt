package pe.edu.upc.routeguard.trip.presentation.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.trip.application.GetLiveTripsUseCase
import pe.edu.upc.routeguard.trip.domain.ArrivalEstimate
import pe.edu.upc.routeguard.trip.domain.LiveTrip
import pe.edu.upc.routeguard.trip.presentation.RoadGuidance
import pe.edu.upc.routeguard.shared.domain.Coordinates
import javax.inject.Inject

data class LiveMonitorUiState(
    val trips: List<LiveTrip> = emptyList(),
    val selectedTripId: String? = null,
    val hasLoaded: Boolean = false,
    /** Road path and arrival estimate of the selected trip only. */
    val roadPath: List<Coordinates>? = null,
    val arrival: ArrivalEstimate? = null,
    val errorMessage: String? = null
) {
    /** The selected trip, or the first one while nothing is selected. */
    val selectedTrip: LiveTrip?
        get() = trips.firstOrNull { it.tripId.value == selectedTripId } ?: trips.firstOrNull()
}

@HiltViewModel
class LiveMonitorViewModel @Inject constructor(
    private val getLiveTrips: GetLiveTripsUseCase,
    private val roadGuidance: RoadGuidance
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveMonitorUiState())
    val uiState: StateFlow<LiveMonitorUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            getLiveTrips()
                .onSuccess { trips ->
                    _uiState.update { it.copy(trips = trips, hasLoaded = true, errorMessage = null) }
                    enrichSelectedTrip()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(hasLoaded = true, errorMessage = e.message) }
                }
        }
    }

    fun onTripSelected(tripId: String) {
        _uiState.update { it.copy(selectedTripId = tripId, roadPath = null, arrival = null) }
        viewModelScope.launch { enrichSelectedTrip() }
    }

    /** Road path and arrival estimate for the selected trip (the others keep straight-line figures). */
    private suspend fun enrichSelectedTrip() {
        val selected = _uiState.value.selectedTrip ?: return
        val path = roadGuidance.pathFor(selected.stops.sortedBy { it.order }.map { it.coordinates })
        val next = selected.nextStop
        val location = selected.location?.coordinates
        val arrival = if (next != null && location != null) roadGuidance.arrivalFor(location, next) else null
        _uiState.update { it.copy(roadPath = path, arrival = arrival) }
    }
}
