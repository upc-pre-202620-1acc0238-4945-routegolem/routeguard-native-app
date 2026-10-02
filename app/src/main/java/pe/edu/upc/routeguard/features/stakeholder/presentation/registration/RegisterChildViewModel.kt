package pe.edu.upc.routeguard.features.stakeholder.presentation.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.features.stakeholder.application.GetParentsUseCase
import pe.edu.upc.routeguard.features.stakeholder.application.RegisterChildUseCase
import pe.edu.upc.routeguard.features.stakeholder.domain.Parent
import javax.inject.Inject

data class RegisterChildUiState(
    val firstName: String = "",
    val lastName: String = "",
    val age: String = "",
    val parents: List<Parent> = emptyList(),
    val selectedParentId: String? = null,
    val isLoading: Boolean = false,
    val registered: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RegisterChildViewModel @Inject constructor(
    private val getParents: GetParentsUseCase,
    private val registerChild: RegisterChildUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterChildUiState())
    val state: StateFlow<RegisterChildUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getParents()
                .onSuccess { parents -> _state.update { it.copy(parents = parents) } }
                .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun onFirstNameChange(v: String) = _state.update { it.copy(firstName = v, errorMessage = null) }
    fun onLastNameChange(v: String) = _state.update { it.copy(lastName = v, errorMessage = null) }
    fun onAgeChange(v: String) = _state.update { it.copy(age = v.filter(Char::isDigit), errorMessage = null) }
    fun onParentSelected(id: String) = _state.update { it.copy(selectedParentId = id, errorMessage = null) }

    fun register() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            registerChild(s.selectedParentId, s.firstName, s.lastName, s.age)
                .onSuccess { _state.update { it.copy(isLoading = false, registered = true) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }
}
