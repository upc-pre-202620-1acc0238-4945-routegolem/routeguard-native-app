package pe.edu.upc.routeguard.features.trip.domain

import pe.edu.upc.routeguard.shared.domain.DomainEvent

sealed interface TripEvent : DomainEvent

data class TripStarted(val tripId: String, override val occurredAt: Long) : TripEvent

data class StudentBoarded(
    val tripId: String,
    val studentId: String,
    override val occurredAt: Long
) : TripEvent

data class TripFinished(val tripId: String, override val occurredAt: Long) : TripEvent

/** Raised when the records saved without signal were delivered to the backend. */
data class OfflineSyncCompleted(
    val tripId: String,
    val syncedLocations: Int,
    val syncedBoardings: Int,
    override val occurredAt: Long
) : TripEvent {
    val total: Int get() = syncedLocations + syncedBoardings
}
