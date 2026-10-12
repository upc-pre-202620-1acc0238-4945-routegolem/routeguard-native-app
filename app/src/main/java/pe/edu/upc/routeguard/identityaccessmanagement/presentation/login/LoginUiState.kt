package pe.edu.upc.routeguard.identityaccessmanagement.presentation.login

import pe.edu.upc.routeguard.identityaccessmanagement.domain.Account

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordHidden: Boolean = true,
    val isLoading: Boolean = false,
    val account: Account? = null,
    val errorMessage: String? = null
)
