package pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.follow

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
import pe.edu.upc.routeguard.core.network.PlatformFeaturesRepository
import pe.edu.upc.routeguard.tripexecutionmonitoring.application.FollowChildTripUseCase
import pe.edu.upc.routeguard.tripexecutionmonitoring.application.GetTripHistoryUseCase
import pe.edu.upc.routeguard.tripexecutionmonitoring.application.ReportEarlyAbsenceUseCase
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.ArrivalEstimate
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LiveTrip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripHistoryItem
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.StudentId
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.RoadGuidance
import pe.edu.upc.routeguard.shared.domain.Coordinates
import javax.inject.Inject

data class EarlyAbsenceRecord(
    val studentName: String,
    val reason: String
)

data class ServiceRating(
    val stars: Int,
    val comment: String
)

data class FollowTripUiState(
    val trip: LiveTrip? = null,
    val tripHistory: List<TripHistoryItem> = emptyList(),
    val isHistoryLoading: Boolean = false,
    val reportedAbsence: EarlyAbsenceRecord? = null,
    val serviceRating: ServiceRating? = null,
    /** True once the first answer arrived, to tell "loading" apart from "no trip in progress". */
    val hasLoaded: Boolean = false,
    val roadPath: List<Coordinates>? = null,
    val arrival: ArrivalEstimate? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class FollowTripViewModel @Inject constructor(
    private val followChildTrip: FollowChildTripUseCase,
    private val getTripHistory: GetTripHistoryUseCase,
    private val reportEarlyAbsence: ReportEarlyAbsenceUseCase,
    private val platformRepo: PlatformFeaturesRepository,
    private val roadGuidance: RoadGuidance,
    private val strings: AppStringsProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(FollowTripUiState())
    val uiState: StateFlow<FollowTripUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            followChildTrip()
                .onSuccess { trip ->
                    val savedRating = trip?.let { platformRepo.getTripRating(it.tripId.value) }
                    _uiState.update {
                        it.copy(
                            trip = trip,
                            hasLoaded = true,
                            errorMessage = null,
                            serviceRating = savedRating?.let { r -> ServiceRating(r.stars, r.comment) } ?: it.serviceRating
                        )
                    }
                    if (trip != null) enrichWithRoads(trip)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(hasLoaded = true, errorMessage = strings.resolveError(e, R.string.error_unexpected)) }
                }
        }
    }

    fun loadTripHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isHistoryLoading = true) }
            getTripHistory()
                .onSuccess { history ->
                    _uiState.update { it.copy(tripHistory = history, isHistoryLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isHistoryLoading = false) }
                }
        }
    }

    fun onReportEarlyAbsence(studentId: StudentId, studentName: String, reason: String) {
        val activeTripId = _uiState.value.trip?.tripId
        viewModelScope.launch {
            reportEarlyAbsence(activeTripId, studentId, studentName, reason)
            _uiState.update {
                it.copy(reportedAbsence = EarlyAbsenceRecord(studentName = studentName, reason = reason))
            }
            refresh()
        }
    }

    fun onSubmitRating(stars: Int, comment: String) {
        val clamped = stars.coerceIn(1, 5)
        val tripId = _uiState.value.trip?.tripId?.value
        _uiState.update {
            it.copy(serviceRating = ServiceRating(stars = clamped, comment = comment.trim()))
        }
        if (tripId != null) {
            viewModelScope.launch {
                platformRepo.rateTrip(tripId, clamped, comment)
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
