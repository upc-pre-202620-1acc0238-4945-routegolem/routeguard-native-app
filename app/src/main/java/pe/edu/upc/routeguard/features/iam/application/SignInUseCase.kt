package pe.edu.upc.routeguard.features.iam.application

import pe.edu.upc.routeguard.features.iam.domain.Account
import pe.edu.upc.routeguard.features.iam.domain.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(email: String, password: String): Result<Account> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa tu correo y contraseña"))
        }
        return repository.signIn(email.trim(), password)
    }
}
