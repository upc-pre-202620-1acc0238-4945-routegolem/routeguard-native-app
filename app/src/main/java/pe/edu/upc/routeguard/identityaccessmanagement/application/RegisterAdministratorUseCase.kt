package pe.edu.upc.routeguard.identityaccessmanagement.application

import pe.edu.upc.routeguard.identityaccessmanagement.domain.Account
import pe.edu.upc.routeguard.identityaccessmanagement.domain.AuthRepository
import javax.inject.Inject

class RegisterAdministratorUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        organizationName: String,
        email: String,
        password: String
    ): Result<Account> {
        if (firstName.isBlank() || lastName.isBlank() || organizationName.isBlank() || email.isBlank()) {
            return Result.failure(IllegalArgumentException("Completa todos los campos"))
        }
        if (password.length < 8) {
            return Result.failure(IllegalArgumentException("La contraseña debe tener al menos 8 caracteres"))
        }
        return repository.registerAdministrator(
            firstName.trim(), lastName.trim(), organizationName.trim(), pe.edu.upc.routeguard.identityaccessmanagement.domain.valueobject.Email(email.trim()), password
        )
    }
}
