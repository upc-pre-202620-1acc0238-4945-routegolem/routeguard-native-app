package pe.edu.upc.routeguard.notificationscommunication.application

import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationRepository
import javax.inject.Inject

class RegisterDeviceTokenUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(userId: String, token: String): Result<Unit> =
        repository.registerDeviceToken(userId, token)
}