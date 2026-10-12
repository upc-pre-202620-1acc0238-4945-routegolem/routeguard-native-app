package pe.edu.upc.routeguard.identityaccessmanagement.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.identityaccessmanagement.application.SignInUseCase
import pe.edu.upc.routeguard.notificationscommunication.application.RegisterDeviceTokenUseCase
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signIn: SignInUseCase,
    private val registerDeviceToken: RegisterDeviceTokenUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(email: String) = _state.update { it.copy(email = email, errorMessage = null) }

    fun onPasswordChange(password: String) = _state.update { it.copy(password = password, errorMessage = null) }

    fun togglePasswordVisibility() = _state.update { it.copy(isPasswordHidden = !it.isPasswordHidden) }

    fun login() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            signIn(current.email, current.password)
                .onSuccess { account ->
                    _state.update { it.copy(isLoading = false, account = account) }
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful && !task.result.isNullOrBlank()) {
                            viewModelScope.launch {
                                registerDeviceToken(account.id.value, task.result)
                            }
                        }
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "No se pudo iniciar sesión")
                    }
                }
        }
    }
}
