package pe.edu.upc.routeguard.stakeholderassetmanagement.presentation.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.stakeholderassetmanagement.application.RegisterParentUseCase
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.AccountCredentials
import javax.inject.Inject

data class RegisterParentUiState(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val credentials: AccountCredentials? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class RegisterParentViewModel @Inject constructor(
    private val registerParent: RegisterParentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterParentUiState())
    val state: StateFlow<RegisterParentUiState> = _state.asStateFlow()

    fun onFirstNameChange(v: String) = _state.update { it.copy(firstName = v, errorMessage = null) }
    fun onLastNameChange(v: String) = _state.update { it.copy(lastName = v, errorMessage = null) }
    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v, errorMessage = null) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v, errorMessage = null) }

    fun register() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            registerParent(s.firstName, s.lastName, s.phone, s.email)
                .onSuccess { provisioned ->
                    _state.update { it.copy(isLoading = false, credentials = provisioned.credentials) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }
}
