package pe.edu.upc.routeguard.stakeholder.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.stakeholder.application.GetDriversUseCase
import pe.edu.upc.routeguard.stakeholder.application.GetGroupsUseCase
import pe.edu.upc.routeguard.stakeholder.application.GetParentsUseCase
import pe.edu.upc.routeguard.stakeholder.domain.Driver
import pe.edu.upc.routeguard.stakeholder.domain.Group
import pe.edu.upc.routeguard.stakeholder.domain.Parent
import javax.inject.Inject

enum class StakeholderTab(val label: String) {
    DRIVERS("Conductores"),
    PARENTS("Padres"),
    GROUPS("Grupos")
}

data class StakeholdersUiState(
    val tab: StakeholderTab = StakeholderTab.DRIVERS,
    val drivers: List<Driver> = emptyList(),
    val parents: List<Parent> = emptyList(),
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class StakeholdersViewModel @Inject constructor(
    private val getDrivers: GetDriversUseCase,
    private val getParents: GetParentsUseCase,
    private val getGroups: GetGroupsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StakeholdersUiState())
    val uiState: StateFlow<StakeholdersUiState> = _uiState.asStateFlow()

    fun onTabSelected(tab: StakeholderTab) = _uiState.update { it.copy(tab = tab) }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val drivers = getDrivers()
            val parents = getParents()
            val groups = getGroups()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    drivers = drivers.getOrDefault(state.drivers),
                    parents = parents.getOrDefault(state.parents),
                    groups = groups.getOrDefault(state.groups),
                    errorMessage = listOf(drivers, parents, groups)
                        .firstNotNullOfOrNull { it.exceptionOrNull()?.message }
                )
            }
        }
    }
}
