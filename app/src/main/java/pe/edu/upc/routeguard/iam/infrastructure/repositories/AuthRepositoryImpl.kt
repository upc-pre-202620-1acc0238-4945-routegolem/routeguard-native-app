package pe.edu.upc.routeguard.iam.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.safeApiCall
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.core.session.UserSession
import pe.edu.upc.routeguard.iam.domain.Account
import pe.edu.upc.routeguard.iam.domain.AuthRepository
import pe.edu.upc.routeguard.iam.domain.Role
import pe.edu.upc.routeguard.iam.domain.SessionToken
import pe.edu.upc.routeguard.iam.infrastructure.mapper.toDomain
import pe.edu.upc.routeguard.iam.infrastructure.mapper.toSession
import pe.edu.upc.routeguard.iam.infrastructure.remote.AuthenticatedUserDto
import pe.edu.upc.routeguard.iam.infrastructure.remote.CreateOrganizationRequestDto
import pe.edu.upc.routeguard.iam.infrastructure.remote.IamApiService
import pe.edu.upc.routeguard.iam.infrastructure.remote.SignInRequestDto
import pe.edu.upc.routeguard.iam.infrastructure.remote.SignUpRequestDto
import pe.edu.upc.routeguard.stakeholder.infrastructure.remote.StakeholderApiService
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val service: IamApiService,
    private val stakeholderService: StakeholderApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun signIn(email: pe.edu.upc.routeguard.iam.domain.valueobject.Email, password: String): Result<Account> =
        safeApiCall { service.signIn(SignInRequestDto(email.value, password)) }
            .mapCatching { dto -> buildAccount(dto).also(::persist) }

    override suspend fun registerAdministrator(
        firstName: String,
        lastName: String,
        organizationName: String,
        email: pe.edu.upc.routeguard.iam.domain.valueobject.Email,
        password: String
    ): Result<Account> {
        val organization = safeApiCall { service.createOrganization(CreateOrganizationRequestDto(organizationName)) }
            .getOrElse { return Result.failure(it) }

        safeApiCall {
            service.signUp(
                SignUpRequestDto(firstName, lastName, email.value, password, ROLE_ADMIN, organization.id)
            )
        }.onFailure { return Result.failure(it) }

        return signIn(email, password)
    }

    override fun currentAccount(): Account? {
        val session = sessionManager.current() ?: return null
        return session.toDomain()
    }

    override fun signOut() = sessionManager.clear()

    /**
     * Drivers and parents act through their stakeholder profile (not the user id), so the profile
     * id is resolved once at sign-in and kept in the session.
     */
    private suspend fun buildAccount(dto: AuthenticatedUserDto): Account {
        val role = Role.from(dto.roleTier)
        val profileId = when (role) {
            Role.DRIVER -> safeApiCall { stakeholderService.getDrivers() }.getOrNull()
                ?.firstOrNull { it.userId == dto.id }?.id

            Role.PARENT -> safeApiCall { stakeholderService.getParents() }.getOrNull()
                ?.firstOrNull { it.userId == dto.id }?.id

            Role.ADMINISTRATOR -> null
        }
        return dto.toDomain(profileId)
    }

    private fun persist(account: Account) {
        sessionManager.save(account.toSession())
    }

    private companion object {
        const val ROLE_ADMIN = "ADMIN"
    }
}
