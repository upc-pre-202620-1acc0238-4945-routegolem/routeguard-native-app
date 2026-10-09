package pe.edu.upc.routeguard.notificationscommunication.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.safeApiCall
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationRepository
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationStatus
import pe.edu.upc.routeguard.notificationscommunication.infrastructure.local.NotificationDao
import pe.edu.upc.routeguard.notificationscommunication.infrastructure.remote.BroadcastRequestDto
import pe.edu.upc.routeguard.notificationscommunication.infrastructure.remote.NotificationApiService
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val service: NotificationApiService,
    private val dao: NotificationDao,
    private val sessionManager: SessionManager
) : NotificationRepository {

    /**
     * Parents only see their own notifications; drivers see the ones of their organization.
     * The local copy is a cache of the signed-in user's list: it is dropped when another user signs in
     * and replaced by the server's list whenever the server answers, so stale rows never linger.
     */
    override suspend fun getNotifications(): List<Notification> {
        val session = sessionManager.current()
        val parentId = if (session?.role == "PARENT") session.profileId else null

        if (session != null && sessionManager.cacheOwner() != session.userId) {
            dao.deleteAll()
            sessionManager.setCacheOwner(session.userId)
        }

        safeApiCall { service.getNotifications(parentId) }
            .onSuccess { dtos ->
                dao.deleteAll()
                dao.upsertNotifications(dtos.map { it.toEntity() })
            }

        return dao.fetchAllNotifications().map { it.toDomain() }
    }

    override suspend fun save(notification: Notification) {
        dao.upsertNotifications(listOf(notification.toEntity()))
    }

    override suspend fun markDelivered(notificationId: String): Result<Unit> =
        safeApiCall { service.markDelivered(notificationId) }
            .map { dao.updateStatus(notificationId, NotificationStatus.DELIVERED.name) }

    override suspend fun triggerPanicAlert(tripId: String): Result<Int> =
        safeApiCall { service.triggerPanicAlert(tripId) }.map { it.notified }

    override suspend fun postBroadcastMessage(tripId: String, message: String): Result<Int> =
        safeApiCall { service.postBroadcastMessage(tripId, BroadcastRequestDto(message)) }.map { it.notified }
}
