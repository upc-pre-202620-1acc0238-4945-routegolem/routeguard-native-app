package pe.edu.upc.routeguard.notifications.domain

import pe.edu.upc.routeguard.notifications.domain.valueobject.GeofenceAlertId
import pe.edu.upc.routeguard.notifications.domain.valueobject.NotificationId
import pe.edu.upc.routeguard.shared.domain.AggregateRoot
import pe.edu.upc.routeguard.shared.domain.BaseEntity
import pe.edu.upc.routeguard.shared.domain.Coordinates
import pe.edu.upc.routeguard.shared.domain.DomainEvent
import pe.edu.upc.routeguard.trip.domain.valueobject.TripId

enum class NotificationStatus {
    PENDING, DISPATCHED, DELIVERED;

    companion object {
        fun from(value: String): NotificationStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
    }
}

enum class NotificationType {
    ANNOUNCEMENT, TRIP_STARTED, PANIC_ALERT, GEOFENCE_BREACHED, STUDENT_BOARDED, INCIDENT_REPORTED, GENERAL;

    companion object {
        fun from(value: String): NotificationType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: GENERAL
    }
}

enum class GeofenceAlertType { APPROACHING_STOP, ARRIVED_AT_SCHOOL }

/** Push message delivered to the device of the parent or the driver. */
data class Notification(
    override val id: NotificationId,
    val tripId: TripId,
    val type: NotificationType,
    val status: NotificationStatus,
    val title: String,
    val body: String,
    val createdAt: Long,
    val hasPanicAlert: Boolean
) : AggregateRoot<NotificationId> {

    /** Alerts generated from an incident or a panic button are high priority. */
    val isHighPriority: Boolean
        get() = hasPanicAlert || type == NotificationType.PANIC_ALERT || type == NotificationType.INCIDENT_REPORTED

    val isDelivered: Boolean get() = status == NotificationStatus.DELIVERED
}

/** Perimeter crossing detected around a stop or the school. */
data class GeofenceAlert(
    override val id: GeofenceAlertId,
    val alertType: GeofenceAlertType,
    val coordinates: Coordinates?,
    val message: String,
    val triggeredAt: Long
) : BaseEntity<GeofenceAlertId>

/** Value object: raw content received from the push provider. */
data class PushPayload(
    val type: String,
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap()
)

data class GeofenceBreached(
    val alert: GeofenceAlert,
    override val occurredAt: Long
) : DomainEvent

data class NotificationDispatched(
    val notificationId: NotificationId,
    override val occurredAt: Long
) : DomainEvent
