package pe.edu.upc.routeguard.features.fleet.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.features.fleet.application.ActivateRouteUseCase
import pe.edu.upc.routeguard.features.fleet.application.ApplyStopOrderUseCase
import pe.edu.upc.routeguard.features.fleet.application.AssignStudentsToRouteUseCase
import pe.edu.upc.routeguard.features.fleet.application.DefineServiceDaysUseCase
import pe.edu.upc.routeguard.features.fleet.application.GetRouteByIdUseCase
import pe.edu.upc.routeguard.features.fleet.application.GetRoutePreviewUseCase
import pe.edu.upc.routeguard.features.fleet.application.GetVehiclesUseCase
import pe.edu.upc.routeguard.features.fleet.application.PickWaypointUseCase
import pe.edu.upc.routeguard.features.fleet.application.ReverseGeocodeUseCase
import pe.edu.upc.routeguard.features.fleet.application.SearchAddressUseCase
import pe.edu.upc.routeguard.features.fleet.application.SelectVehicleUseCase
import pe.edu.upc.routeguard.features.fleet.application.SetDepartureTimeUseCase
import pe.edu.upc.routeguard.features.fleet.application.SuggestStopOrderUseCase
import pe.edu.upc.routeguard.features.fleet.domain.Route
import pe.edu.upc.routeguard.features.fleet.domain.ServiceDay
import pe.edu.upc.routeguard.features.fleet.domain.Vehicle
import pe.edu.upc.routeguard.features.fleet.domain.Waypoint
import pe.edu.upc.routeguard.features.stakeholder.application.GetChildrenUseCase
import pe.edu.upc.routeguard.features.stakeholder.application.GetDriversUseCase
import pe.edu.upc.routeguard.features.stakeholder.application.GetGroupsUseCase
import pe.edu.upc.routeguard.features.stakeholder.domain.Child
import pe.edu.upc.routeguard.features.stakeholder.domain.Driver
import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.shared.domain.Place
import java.util.Locale
import javax.inject.Inject

data class RouteDetailUiState(
    val route: Route? = null,
    val vehicles: List<Vehicle> = emptyList(),
    val drivers: List<Driver> = emptyList(),
    /** Students that come from groups already finalized in Stakeholder. */
    val assignableStudents: List<Child> = emptyList(),
    val waypointName: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val searchQuery: String = "",
    val searchResults: List<Place> = emptyList(),
    val isSearching: Boolean = false,
    /** Changes when the map must frame the picked point again (search result chosen). */
    val mapFocusKey: Int = 0,
    /** Path along the roads through the stops (Mapbox), straight lines while it is null. */
    val roadPath: List<Coordinates>? = null,
    /** Best order proposed by Mapbox, waiting for the administrator to apply or discard it. */
    val suggestedOrder: List<Waypoint>? = null,
    val selectedStudentIds: Set<String> = emptySet(),
    val selectedVehicleId: String? = null,
    val selectedDriverId: String? = null,
    val selectedDays: Set<ServiceDay> = emptySet(),
    val departureTime: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
) {
    /** Point typed or tapped for the next stop, shown on the map. */
    val pickedPoint: Coordinates?
        get() {
            val lat = latitude.trim().toDoubleOrNull() ?: return null
            val lng = longitude.trim().toDoubleOrNull() ?: return null
            return if (lat in -90.0..90.0 && lng in -180.0..180.0) Coordinates(lat, lng) else null
        }
}

@HiltViewModel
class RouteDetailViewModel @Inject constructor(
    private val getRouteById: GetRouteByIdUseCase,
    private val getVehicles: GetVehiclesUseCase,
    private val pickWaypoint: PickWaypointUseCase,
    private val assignStudents: AssignStudentsToRouteUseCase,
    private val selectVehicle: SelectVehicleUseCase,
    private val defineServiceDays: DefineServiceDaysUseCase,
    private val setDepartureTime: SetDepartureTimeUseCase,
    private val activateRoute: ActivateRouteUseCase,
    private val searchAddress: SearchAddressUseCase,
    private val reverseGeocode: ReverseGeocodeUseCase,
    private val getRoutePreview: GetRoutePreviewUseCase,
    private val suggestStopOrder: SuggestStopOrderUseCase,
    private val applyStopOrder: ApplyStopOrderUseCase,
    private val getDrivers: GetDriversUseCase,
    private val getGroups: GetGroupsUseCase,
    private val getChildren: GetChildrenUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RouteDetailUiState())
    val state: StateFlow<RouteDetailUiState> = _state.asStateFlow()

    fun load(routeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val route = getRouteById(routeId)
            val vehicles = getVehicles()
            val drivers = getDrivers()
            val groups = getGroups()
            val children = getChildren()

            val finalizedStudentIds = groups.getOrDefault(emptyList())
                .filter { it.isFinalized }
                .flatMap { it.studentIds }
                .toSet()

            _state.update { s ->
                val loaded = route.getOrNull()
                s.copy(
                    isLoading = false,
                    route = loaded ?: s.route,
                    vehicles = vehicles.getOrDefault(emptyList()),
                    drivers = drivers.getOrDefault(emptyList()),
                    assignableStudents = children.getOrDefault(emptyList())
                        .filter { it.id in finalizedStudentIds },
                    selectedStudentIds = loaded?.studentIds?.toSet() ?: s.selectedStudentIds,
                    selectedVehicleId = loaded?.vehicle?.let { v ->
                        vehicles.getOrDefault(emptyList()).firstOrNull { it.plate == v.plate }?.id
                    } ?: s.selectedVehicleId,
                    selectedDriverId = loaded?.driverId ?: s.selectedDriverId,
                    selectedDays = loaded?.serviceDays ?: s.selectedDays,
                    departureTime = loaded?.departureTime ?: s.departureTime,
                    errorMessage = route.exceptionOrNull()?.message
                )
            }
            route.getOrNull()?.let { refreshRoadPath(it) }
        }
    }

    // ---- form fields ----

    fun onWaypointNameChange(v: String) = _state.update { it.copy(waypointName = v) }
    fun onLatitudeChange(v: String) = _state.update { it.copy(latitude = v) }
    fun onLongitudeChange(v: String) = _state.update { it.copy(longitude = v) }
    fun onDepartureTimeChange(v: String) = _state.update { it.copy(departureTime = v) }
    fun onVehicleSelected(id: String) = _state.update { it.copy(selectedVehicleId = id) }
    fun onDriverSelected(id: String) = _state.update { it.copy(selectedDriverId = id) }
    fun onSearchQueryChange(v: String) = _state.update { it.copy(searchQuery = v) }

    fun toggleStudent(id: String) = _state.update {
        it.copy(selectedStudentIds = if (id in it.selectedStudentIds) it.selectedStudentIds - id else it.selectedStudentIds + id)
    }

    fun toggleDay(day: ServiceDay) = _state.update {
        it.copy(selectedDays = if (day in it.selectedDays) it.selectedDays - day else it.selectedDays + day)
    }

    // ---- map: pick, search, name ----

    /** Tap on the map: fills the coordinates and suggests the name of the place for the stop. */
    fun onMapPicked(point: Coordinates) {
        _state.update {
            it.copy(
                latitude = String.format(Locale.US, "%.6f", point.latitude),
                longitude = String.format(Locale.US, "%.6f", point.longitude)
            )
        }
        viewModelScope.launch {
            val place = reverseGeocode(point).getOrNull() ?: return@launch
            _state.update {
                if (it.waypointName.isBlank()) {
                    it.copy(waypointName = place.name.ifBlank { place.address })
                } else {
                    it
                }
            }
        }
    }

    fun onSearch() {
        val s = _state.value
        val near = s.pickedPoint ?: s.route?.waypoints?.lastOrNull()?.coordinates
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, errorMessage = null) }
            searchAddress(s.searchQuery, near)
                .onSuccess { places ->
                    _state.update {
                        it.copy(
                            isSearching = false,
                            searchResults = places,
                            errorMessage = if (places.isEmpty()) "No se encontraron resultados" else null
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(isSearching = false, errorMessage = e.message) } }
        }
    }

    fun onPlaceSelected(place: Place) = _state.update {
        it.copy(
            waypointName = place.name.ifBlank { place.address },
            latitude = String.format(Locale.US, "%.6f", place.coordinates.latitude),
            longitude = String.format(Locale.US, "%.6f", place.coordinates.longitude),
            searchResults = emptyList(),
            searchQuery = "",
            mapFocusKey = it.mapFocusKey + 1
        )
    }

    // ---- route edition ----

    fun addWaypoint() {
        val s = _state.value
        val route = s.route ?: return
        execute("Parada agregada", onSuccess = {
            _state.update { st -> st.copy(waypointName = "", latitude = "", longitude = "", suggestedOrder = null) }
        }) {
            pickWaypoint(route.id, s.waypointName, s.latitude, s.longitude)
        }
    }

    /** US-04: asks Mapbox for the best order of the stops (first and last stay where they are). */
    fun suggestOrder() {
        val route = _state.value.route ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null, errorMessage = null) }
            suggestStopOrder(route)
                .onSuccess { proposal -> _state.update { it.copy(isLoading = false, suggestedOrder = proposal) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    fun applySuggestedOrder() {
        val s = _state.value
        val route = s.route ?: return
        val proposal = s.suggestedOrder ?: return
        execute("Orden de paradas actualizado", onSuccess = {
            _state.update { it.copy(suggestedOrder = null) }
        }) { applyStopOrder(route, proposal) }
    }

    fun discardSuggestedOrder() = _state.update { it.copy(suggestedOrder = null) }

    fun saveStudents() {
        val s = _state.value
        val route = s.route ?: return
        execute("Estudiantes asignados") { assignStudents(route, s.selectedStudentIds) }
    }

    fun saveVehicle() {
        val s = _state.value
        val route = s.route ?: return
        val vehicle = s.vehicles.firstOrNull { it.id == s.selectedVehicleId }
        execute("Vehículo y conductor asignados") { selectVehicle(route.id, vehicle, s.selectedDriverId) }
    }

    fun saveServiceDays() {
        val s = _state.value
        val route = s.route ?: return
        execute("Días de servicio definidos") { defineServiceDays(route.id, s.selectedDays) }
    }

    fun saveDepartureTime() {
        val s = _state.value
        val route = s.route ?: return
        execute("Hora de salida definida") { setDepartureTime(route.id, s.departureTime) }
    }

    fun activate() {
        val route = _state.value.route ?: return
        execute("Ruta activada") { activateRoute(route) }
    }

    /** Path along the roads through the current stops; straight lines if Mapbox does not answer. */
    private suspend fun refreshRoadPath(route: Route) {
        val path = if (route.waypoints.size >= 2) getRoutePreview(route).getOrNull()?.path else null
        _state.update { it.copy(roadPath = path) }
    }

    private fun execute(
        successMessage: String,
        onSuccess: () -> Unit = {},
        call: suspend () -> Result<Route>
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null, errorMessage = null) }
            call()
                .onSuccess { route ->
                    _state.update { it.copy(isLoading = false, route = route, message = successMessage) }
                    onSuccess()
                    refreshRoadPath(route)
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }
}
