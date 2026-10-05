package pe.edu.upc.routeguard.trip.presentation.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.trip.application.BoardStudentUseCase
import pe.edu.upc.routeguard.trip.application.CountPendingRecordsUseCase
import pe.edu.upc.routeguard.trip.application.FinishTripUseCase
import pe.edu.upc.routeguard.trip.application.GetActiveTripUseCase
import pe.edu.upc.routeguard.trip.application.GetAssignedRoutesUseCase
import pe.edu.upc.routeguard.trip.application.GetLastKnownLocationUseCase
import pe.edu.upc.routeguard.trip.application.ReportIncidentUseCase
import pe.edu.upc.routeguard.trip.application.RouteTrackingService
import pe.edu.upc.routeguard.trip.application.StartTripUseCase
import pe.edu.upc.routeguard.trip.application.SyncOfflineRecordsUseCase
import pe.edu.upc.routeguard.trip.domain.BoardingState
import pe.edu.upc.routeguard.trip.domain.IncidentType
import pe.edu.upc.routeguard.trip.domain.RouteProgress
import pe.edu.upc.routeguard.trip.domain.Trip
import pe.edu.upc.routeguard.trip.domain.valueobject.RouteId
import pe.edu.upc.routeguard.trip.domain.valueobject.StudentId
import pe.edu.upc.routeguard.trip.presentation.RoadGuidance
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val getAssignedRoutes: GetAssignedRoutesUseCase,
    private val getActiveTrip: GetActiveTripUseCase,
    private val startTrip: StartTripUseCase,
    private val boardStudent: BoardStudentUseCase,
    private val reportIncident: ReportIncidentUseCase,
    private val finishTrip: FinishTripUseCase,
    private val syncOfflineRecords: SyncOfflineRecordsUseCase,
    private val countPendingRecords: CountPendingRecordsUseCase,
    private val getLastKnownLocation: GetLastKnownLocationUseCase,
    private val routeTrackingService: RouteTrackingService,
    private val roadGuidance: RoadGuidance
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val trip = getActiveTrip()
            val routes = if (trip == null) getAssignedRoutes() else null

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    trip = trip,
                    visitedStops = if (trip == null) emptySet() else state.visitedStops,
                    assignedRoutes = routes?.getOrDefault(state.assignedRoutes) ?: emptyList(),
                    pendingRecords = trip?.let { countPendingRecords(it.id) } ?: 0,
                    errorMessage = routes?.exceptionOrNull()?.message
                )
            }

            if (trip != null) {
                val path = roadGuidance.pathFor(trip.stops.sortedBy { it.order }.map { it.coordinates })
                _uiState.update { it.copy(roadPath = path) }
            }
        }
    }

    /** Moves the driver marker on the map with the last point saved by the background GPS. */
    fun refreshLocation() {
        val trip = _uiState.value.trip ?: return
        viewModelScope.launch {
            val location = getLastKnownLocation(trip.id)
            _uiState.update {
                it.copy(
                    currentLocation = location,
                    visitedStops = RouteProgress.visited(trip.stops, location, it.visitedStops)
                )
            }

            // Real distance/time by road to the next stop (throttled inside RoadGuidance).
            val state = _uiState.value
            val next = RouteProgress.nextStop(trip.stops, state.visitedStops, location, null)
            if (location != null && next != null) {
                val estimate = roadGuidance.arrivalFor(location, next)
                _uiState.update { it.copy(arrival = estimate) }
            }
        }
    }

    /** Select Route Card + Start Trip. */
    fun onStartTrip(routeId: RouteId) {
        execute("Viaje iniciado") { startTrip(routeId) }
    }

    fun onBoardingStatus(studentId: StudentId, state: BoardingState) {
        val trip = _uiState.value.trip ?: return
        execute(null) { boardStudent(trip, studentId, state) }
    }

    fun onReportIncident(type: IncidentType, description: String) {
        val trip = _uiState.value.trip ?: return
        viewModelScope.launch {
            reportIncident(trip, type, description)
                .onSuccess { _uiState.update { it.copy(message = "Incidencia reportada", errorMessage = null) } }
                .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun onSyncOffline() {
        val trip = _uiState.value.trip ?: return
        viewModelScope.launch {
            syncOfflineRecords(trip.id)
                .onSuccess { event ->
                    _uiState.update {
                        it.copy(message = "${event.total} registros sincronizados", errorMessage = null)
                    }
                }
                .onFailure { _uiState.update { it.copy(errorMessage = "Sin señal: se enviarán al reconectar") } }
            _uiState.update { it.copy(pendingRecords = countPendingRecords(trip.id)) }
        }
    }

    fun onFinishTrip() {
        val trip = _uiState.value.trip ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            finishTrip(trip)
                .onSuccess {
                    routeTrackingService.stop()
                    _uiState.update { it.copy(message = "Ruta finalizada") }
                    refresh()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    /** Called by the screen once the location permission state is known. */
    fun onLocationPermissionResult(granted: Boolean) {
        val trip = _uiState.value.trip
        if (granted && trip?.acceptsRecords == true) {
            routeTrackingService.start()
        } else if (trip == null || !trip.isActive) {
            routeTrackingService.stop()
        }
    }

    private fun execute(successMessage: String?, call: suspend () -> Result<Trip>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null, errorMessage = null) }
            call()
                .onSuccess { trip ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            trip = trip,
                            message = successMessage,
                            pendingRecords = countPendingRecords(trip.id)
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }
}
