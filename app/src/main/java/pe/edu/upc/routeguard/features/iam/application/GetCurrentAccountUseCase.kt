package pe.edu.upc.routeguard.features.iam.application

import pe.edu.upc.routeguard.features.iam.domain.AuthRepository
import javax.inject.Inject

class GetCurrentAccountUseCase @Inject constructor(private val repository: AuthRepository) {

    operator fun invoke() = repository.currentAccount()
}
