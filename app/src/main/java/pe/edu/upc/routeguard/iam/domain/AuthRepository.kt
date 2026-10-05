package pe.edu.upc.routeguard.iam.domain

import pe.edu.upc.routeguard.iam.domain.valueobject.Email

interface AuthRepository {
    suspend fun signIn(email: Email, password: String): Result<Account>

    /** Creates the organization (tenant), its administrator account and signs in. */
    suspend fun registerAdministrator(
        firstName: String,
        lastName: String,
        organizationName: String,
        email: Email,
        password: String
    ): Result<Account>

    fun currentAccount(): Account?

    fun signOut()
}
