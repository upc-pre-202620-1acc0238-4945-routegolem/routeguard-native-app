package pe.edu.upc.routeguard.shared.domain

/** Entity identified by a unique id (Shared Bounded Context). */
interface BaseEntity<ID : Any> {
    val id: ID
}

/** Marker for aggregate roots, the only entities repositories work with. */
interface AggregateRoot<ID : Any> : BaseEntity<ID>

/** Marker for domain events raised by aggregates. */
interface DomainEvent {
    val occurredAt: Long
}
