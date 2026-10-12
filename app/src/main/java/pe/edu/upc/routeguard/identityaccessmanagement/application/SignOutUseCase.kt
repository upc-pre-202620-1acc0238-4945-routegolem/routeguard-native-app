package pe.edu.upc.routeguard.identityaccessmanagement.application

import pe.edu.upc.routeguard.identityaccessmanagement.domain.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(private val repository: AuthRepository) {

    operator fun invoke() = repository.signOut()
}
