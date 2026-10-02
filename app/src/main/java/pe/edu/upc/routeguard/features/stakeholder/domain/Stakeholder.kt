package pe.edu.upc.routeguard.features.stakeholder.domain

import pe.edu.upc.routeguard.shared.domain.AggregateRoot
import pe.edu.upc.routeguard.shared.domain.BaseEntity

/** Person who operates a transport unit. [userId] is the IAM account used to sign in. */
data class Driver(
    override val id: String,
    val userId: String,
    val fullName: String,
    val phone: String,
    val licenseNumber: String,
    val email: String
) : AggregateRoot<String>

/** Student linked to an already registered parent. */
data class Child(
    override val id: String,
    val fullName: String,
    val age: Int,
    val parentId: String
) : BaseEntity<String>

/** Person responsible for one or more students. */
data class Parent(
    override val id: String,
    val userId: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val children: List<Child>
) : AggregateRoot<String>

enum class GroupStatus { DRAFT, FINALIZED }

/** Set of students that is later assigned to a route. */
data class Group(
    override val id: String,
    val name: String,
    val studentIds: List<String>,
    val status: GroupStatus
) : AggregateRoot<String> {
    val isFinalized: Boolean get() = status == GroupStatus.FINALIZED
}

/** Automatic credentials provisioned for driver and parent accounts. */
data class AccountCredentials(
    val email: String,
    val temporaryPassword: String
)

data class Provisioned<T>(
    val profile: T,
    val credentials: AccountCredentials
)
