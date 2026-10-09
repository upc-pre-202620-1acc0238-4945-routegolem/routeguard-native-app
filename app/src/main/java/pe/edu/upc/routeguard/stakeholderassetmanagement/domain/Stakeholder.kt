package pe.edu.upc.routeguard.stakeholderassetmanagement.domain

import pe.edu.upc.routeguard.shared.domain.AggregateRoot
import pe.edu.upc.routeguard.shared.domain.BaseEntity
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.ChildId
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.DriverId
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.Email
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.GroupId
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.LicenseNumber
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.ParentId
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.Phone
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.StudentId
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.UserId

/** Person who operates a transport unit. [userId] is the IAM account used to sign in. */
data class Driver(
    override val id: DriverId,
    val userId: UserId,
    val fullName: String,
    val phone: Phone,
    val licenseNumber: LicenseNumber,
    val email: Email
) : AggregateRoot<DriverId>

/** Student linked to an already registered parent. */
data class Child(
    override val id: ChildId,
    val fullName: String,
    val age: Int,
    val parentId: ParentId
) : BaseEntity<ChildId>

/** Person responsible for one or more students. */
data class Parent(
    override val id: ParentId,
    val userId: UserId,
    val fullName: String,
    val phone: Phone,
    val email: Email,
    val children: List<Child>
) : AggregateRoot<ParentId>

enum class GroupStatus { DRAFT, FINALIZED }

/** Set of students that is later assigned to a route. */
data class Group(
    override val id: GroupId,
    val name: String,
    val studentIds: List<StudentId>,
    val status: GroupStatus
) : AggregateRoot<GroupId> {
    val isFinalized: Boolean get() = status == GroupStatus.FINALIZED
}

/** Automatic credentials provisioned for driver and parent accounts. */
data class AccountCredentials(
    val email: Email,
    val temporaryPassword: String
)

data class Provisioned<T>(
    val profile: T,
    val credentials: AccountCredentials
)
