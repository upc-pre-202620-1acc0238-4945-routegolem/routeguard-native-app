package pe.edu.upc.routeguard.tripexecutionmonitoring.domain

import pe.edu.upc.routeguard.shared.domain.DomainEvent
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.StudentId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.TripId

sealed interface TripEvent : DomainEvent

data class TripStarted(val tripId: TripId, override val occurredAt: Long) : TripEvent

data class StudentBoarded(
    val tripId: TripId,
    val studentId: StudentId,
    override val occurredAt: Long
) : TripEvent

data class TripFinished(val tripId: TripId, override val occurredAt: Long) : TripEvent

/** Raised when the records saved without signal were delivered to the backend. */
data class OfflineSyncCompleted(
    val tripId: TripId,
    val syncedLocations: Int,
    val syncedBoardings: Int,
    override val occurredAt: Long
) : TripEvent {
    val total: Int get() = syncedLocations + syncedBoardings
}
