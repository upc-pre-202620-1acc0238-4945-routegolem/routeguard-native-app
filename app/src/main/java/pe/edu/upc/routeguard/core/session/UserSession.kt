package pe.edu.upc.routeguard.core.session

data class UserSession(
    val userId: String,
    val fullName: String,
    val email: String,
    val role: String,
    val token: String,
    val organizationId: String,
    /** Driver id or parent id of the signed-in user (null for administrators). */
    val profileId: String?
)
