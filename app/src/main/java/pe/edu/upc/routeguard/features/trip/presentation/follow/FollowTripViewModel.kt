package pe.edu.upc.routeguard.features.trip.presentation.follow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.features.trip.application.FollowChildTripUseCase
import pe.edu.upc.routeguard.features.trip.domain.ArrivalEstimate
import pe.edu.upc.routeguard.features.trip.domain.LiveTrip
import pe.edu.upc.routeguard.features.trip.presentation.RoadGuidance
import pe.edu.upc.routeguard.shared.domain.Coordinates
import javax.inject.Inject

data class FollowTripUiState(
    val trip: LiveTrip? = null,
    /** True once the first answer arrived, to tell "loading" apart from "no trip in progress". */
    val hasLoaded: Boolean = false,
    val roadPath: List<Coordinates>? = null,
    val arrival: ArrivalEstimate? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class FollowTripViewModel @Inject constructor(
    private val followChildTrip: FollowChildTripUseCase,
    private val roadGuidance: RoadGuidance
) : ViewModel() {

    private val _uiState = MutableStateFlow(FollowTripUiState())
    val uiState: StateFlow<FollowTripUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            followChildTrip()
                .onSuccess { trip ->
                    _uiState.update { it.copy(trip = trip, hasLoaded = true, errorMessage = null) }
                    if (trip != null) enrichWithRoads(trip)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(hasLoaded = true, errorMessage = e.message) }
                }
        }
    }

    /** Road path through the stops and the road distance/time from the vehicle to the next stop. */
    private suspend fun enrichWithRoads(trip: LiveTrip) {
        val path = roadGuidance.pathFor(trip.stops.sortedBy { it.order }.map { it.coordinates })
        val next = trip.nextStop
        val location = trip.location?.coordinates
        val arrival = if (next != null && location != null) roadGuidance.arrivalFor(location, next) else null
        _uiState.update { it.copy(roadPath = path, arrival = arrival) }
    }
}
