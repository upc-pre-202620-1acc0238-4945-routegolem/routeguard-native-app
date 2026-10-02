package pe.edu.upc.routeguard.features.iam.domain

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<Account>

    /** Creates the organization (tenant), its administrator account and signs in. */
    suspend fun registerAdministrator(
        firstName: String,
        lastName: String,
        organizationName: String,
        email: String,
        password: String
    ): Result<Account>

    fun currentAccount(): Account?

    fun signOut()
}
