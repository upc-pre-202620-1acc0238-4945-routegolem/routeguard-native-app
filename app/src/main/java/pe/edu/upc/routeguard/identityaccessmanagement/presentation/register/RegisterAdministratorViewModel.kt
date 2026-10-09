package pe.edu.upc.routeguard.identityaccessmanagement.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.identityaccessmanagement.application.RegisterAdministratorUseCase
import pe.edu.upc.routeguard.identityaccessmanagement.domain.Account
import javax.inject.Inject

data class RegisterAdministratorUiState(
    val firstName: String = "",
    val lastName: String = "",
    val organizationName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val account: Account? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class RegisterAdministratorViewModel @Inject constructor(
    private val registerAdministrator: RegisterAdministratorUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterAdministratorUiState())
    val state: StateFlow<RegisterAdministratorUiState> = _state.asStateFlow()

    fun onFirstNameChange(v: String) = _state.update { it.copy(firstName = v, errorMessage = null) }
    fun onLastNameChange(v: String) = _state.update { it.copy(lastName = v, errorMessage = null) }
    fun onOrganizationChange(v: String) = _state.update { it.copy(organizationName = v, errorMessage = null) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v, errorMessage = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, errorMessage = null) }

    fun register() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            registerAdministrator(s.firstName, s.lastName, s.organizationName, s.email, s.password)
                .onSuccess { account ->
                    _state.update { it.copy(isLoading = false, account = account) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "No se pudo registrar")
                    }
                }
        }
    }
}
