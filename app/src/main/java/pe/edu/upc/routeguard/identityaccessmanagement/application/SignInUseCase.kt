package pe.edu.upc.routeguard.identityaccessmanagement.application

import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.localization.LocalizedException
import pe.edu.upc.routeguard.identityaccessmanagement.domain.Account
import pe.edu.upc.routeguard.identityaccessmanagement.domain.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(email: String, password: String): Result<Account> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(LocalizedException(R.string.error_credentials_required, fallback = "Ingresa tu correo y contraseña"))
        }
        return repository.signIn(pe.edu.upc.routeguard.identityaccessmanagement.domain.valueobject.Email(email.trim()), password)
    }
}
