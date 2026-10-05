package pe.edu.upc.routeguard.fleet.presentation.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.fleet.application.DefineRouteUseCase
import pe.edu.upc.routeguard.fleet.application.GetRoutesUseCase
import pe.edu.upc.routeguard.fleet.domain.Route
import javax.inject.Inject

data class RoutesUiState(
    val routes: List<Route> = emptyList(),
    val newRouteName: String = "",
    val isLoading: Boolean = false,
    val createdRouteId: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class RoutesViewModel @Inject constructor(
    private val getRoutes: GetRoutesUseCase,
    private val defineRoute: DefineRouteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutesUiState())
    val uiState: StateFlow<RoutesUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, createdRouteId = null) }
            getRoutes()
                .onSuccess { routes -> _uiState.update { it.copy(isLoading = false, routes = routes) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    fun onNewRouteNameChange(value: String) = _uiState.update { it.copy(newRouteName = value) }

    fun createRoute() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            defineRoute(_uiState.value.newRouteName)
                .onSuccess { route ->
                    _uiState.update {
                        it.copy(isLoading = false, newRouteName = "", createdRouteId = route.id.value)
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    fun onNavigatedToCreated() = _uiState.update { it.copy(createdRouteId = null) }
}
