package pe.edu.upc.routeguard.iam.presentation.login

import pe.edu.upc.routeguard.iam.domain.Account

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordHidden: Boolean = true,
    val isLoading: Boolean = false,
    val account: Account? = null,
    val errorMessage: String? = null
)
