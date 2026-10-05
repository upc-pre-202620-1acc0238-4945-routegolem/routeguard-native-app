package pe.edu.upc.routeguard.stakeholder.presentation.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.stakeholder.application.RegisterDriverUseCase
import pe.edu.upc.routeguard.stakeholder.domain.AccountCredentials
import javax.inject.Inject

data class RegisterDriverUiState(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val licenseNumber: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val credentials: AccountCredentials? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class RegisterDriverViewModel @Inject constructor(
    private val registerDriver: RegisterDriverUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterDriverUiState())
    val state: StateFlow<RegisterDriverUiState> = _state.asStateFlow()

    fun onFirstNameChange(v: String) = _state.update { it.copy(firstName = v, errorMessage = null) }
    fun onLastNameChange(v: String) = _state.update { it.copy(lastName = v, errorMessage = null) }
    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v, errorMessage = null) }
    fun onLicenseChange(v: String) = _state.update { it.copy(licenseNumber = v, errorMessage = null) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v, errorMessage = null) }

    fun register() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            registerDriver(s.firstName, s.lastName, s.phone, s.licenseNumber, s.email)
                .onSuccess { provisioned ->
                    _state.update { it.copy(isLoading = false, credentials = provisioned.credentials) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }
}
