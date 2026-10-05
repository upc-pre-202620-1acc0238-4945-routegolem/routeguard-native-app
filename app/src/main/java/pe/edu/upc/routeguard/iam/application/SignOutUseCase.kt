package pe.edu.upc.routeguard.iam.application

import pe.edu.upc.routeguard.iam.domain.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(private val repository: AuthRepository) {

    operator fun invoke() = repository.signOut()
}
