package pe.edu.upc.routeguard.identityaccessmanagement.domain

import pe.edu.upc.routeguard.identityaccessmanagement.domain.valueobject.AccountId
import pe.edu.upc.routeguard.identityaccessmanagement.domain.valueobject.Email
import pe.edu.upc.routeguard.identityaccessmanagement.domain.valueobject.OrganizationId
import pe.edu.upc.routeguard.identityaccessmanagement.domain.valueobject.ProfileId
import pe.edu.upc.routeguard.shared.domain.AggregateRoot

/** Role that determines which functionalities are visible for a profile. */
enum class Role {
    ADMINISTRATOR, DRIVER, PARENT;

    companion object {
        /** Maps the backend role tier (ADMIN, DRIVER, PARENT). */
        fun from(value: String): Role = when (value.trim().uppercase()) {
            "ADMIN", "ADMINISTRATOR" -> ADMINISTRATOR
            "DRIVER" -> DRIVER
            else -> PARENT
        }
    }
}

/** Opaque credential issued after a valid authentication (JWT). */
@JvmInline
value class SessionToken(val value: String)

/** Aggregate root: unique identity of a user of the platform. */
data class Account(
    override val id: AccountId,
    val fullName: String,
    val email: Email,
    val role: Role,
    val token: SessionToken,
    val organizationId: OrganizationId,
    val profileId: ProfileId?
) : AggregateRoot<AccountId>
