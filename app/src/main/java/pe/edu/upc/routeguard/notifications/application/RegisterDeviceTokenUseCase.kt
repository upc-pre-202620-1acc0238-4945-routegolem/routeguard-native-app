package pe.edu.upc.routeguard.notifications.application

import pe.edu.upc.routeguard.notifications.domain.NotificationRepository
import javax.inject.Inject

class RegisterDeviceTokenUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(userId: String, token: String): Result<Unit>{
        return repository.registerDeviceToken(userId, token)
    }
}