package pe.edu.upc.routeguard.features.notifications.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.ApiDates
import pe.edu.upc.routeguard.features.notifications.domain.Notification
import pe.edu.upc.routeguard.features.notifications.domain.NotificationStatus
import pe.edu.upc.routeguard.features.notifications.domain.NotificationType
import pe.edu.upc.routeguard.features.notifications.infrastructure.local.NotificationEntity
import pe.edu.upc.routeguard.features.notifications.infrastructure.remote.NotificationDto

private fun titleFor(type: NotificationType) = when (type) {
    NotificationType.ANNOUNCEMENT -> "Aviso del conductor"
    NotificationType.TRIP_STARTED -> "Viaje iniciado"
    NotificationType.PANIC_ALERT -> "¡Alerta de pánico!"
    NotificationType.GEOFENCE_BREACHED -> "El vehículo está cerca"
    NotificationType.STUDENT_BOARDED -> "Estudiante a bordo"
    NotificationType.INCIDENT_REPORTED -> "Incidencia en el viaje"
    NotificationType.GENERAL -> "Notificación"
}

fun NotificationDto.toEntity(): NotificationEntity {
    val type = NotificationType.from(category)
    return NotificationEntity(
        id = id,
        tripId = tripId,
        type = type.name,
        status = NotificationStatus.from(deliveryState).name,
        title = titleFor(type),
        body = message,
        createdAt = ApiDates.parse(sentAt) ?: System.currentTimeMillis(),
        hasPanicAlert = alerts.orEmpty().any { it.panic }
    )
}

fun NotificationEntity.toDomain() = Notification(
    id = id,
    tripId = tripId,
    type = NotificationType.from(type),
    status = NotificationStatus.from(status),
    title = title,
    body = body,
    createdAt = createdAt,
    hasPanicAlert = hasPanicAlert
)

fun Notification.toEntity() = NotificationEntity(
    id = id,
    tripId = tripId,
    type = type.name,
    status = status.name,
    title = title,
    body = body,
    createdAt = createdAt,
    hasPanicAlert = hasPanicAlert
)
