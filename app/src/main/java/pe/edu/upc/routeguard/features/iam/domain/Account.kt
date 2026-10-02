package pe.edu.upc.routeguard.features.iam.domain

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
    override val id: String,
    val fullName: String,
    val email: String,
    val role: Role,
    val token: SessionToken,
    val organizationId: String,
    val profileId: String?
) : AggregateRoot<String>
