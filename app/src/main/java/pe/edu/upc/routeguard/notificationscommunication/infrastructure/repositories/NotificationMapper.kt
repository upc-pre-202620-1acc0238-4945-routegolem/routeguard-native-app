package pe.edu.upc.routeguard.notificationscommunication.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.ApiDates
import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationStatus
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationType
import pe.edu.upc.routeguard.notificationscommunication.domain.valueobject.NotificationId
import pe.edu.upc.routeguard.notificationscommunication.infrastructure.local.NotificationEntity
import pe.edu.upc.routeguard.notificationscommunication.infrastructure.remote.NotificationDto
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.TripId

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
    val type = NotificationType.from(category ?: "")
    return NotificationEntity(
        id = id.value,
        tripId = tripId?.value ?: "",
        type = type.name,
        status = NotificationStatus.from(deliveryState ?: "").name,
        title = titleFor(type),
        body = message ?: "",
        createdAt = ApiDates.parse(sentAt) ?: System.currentTimeMillis(),
        hasPanicAlert = alerts.orEmpty().any { it.panic }
    )
}

fun NotificationEntity.toDomain() = Notification(
    id = NotificationId(id),
    tripId = TripId(tripId),
    type = NotificationType.from(type),
    status = NotificationStatus.from(status),
    title = title,
    body = body,
    createdAt = createdAt,
    hasPanicAlert = hasPanicAlert
)

fun Notification.toEntity() = NotificationEntity(
    id = id.value,
    tripId = tripId.value,
    type = type.name,
    status = status.name,
    title = title,
    body = body,
    createdAt = createdAt,
    hasPanicAlert = hasPanicAlert
)
