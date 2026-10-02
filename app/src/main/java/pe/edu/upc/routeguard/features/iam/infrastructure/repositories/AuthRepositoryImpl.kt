package pe.edu.upc.routeguard.features.iam.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.safeApiCall
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.core.session.UserSession
import pe.edu.upc.routeguard.features.iam.domain.Account
import pe.edu.upc.routeguard.features.iam.domain.AuthRepository
import pe.edu.upc.routeguard.features.iam.domain.Role
import pe.edu.upc.routeguard.features.iam.domain.SessionToken
import pe.edu.upc.routeguard.features.iam.infrastructure.remote.AuthenticatedUserDto
import pe.edu.upc.routeguard.features.iam.infrastructure.remote.CreateOrganizationRequestDto
import pe.edu.upc.routeguard.features.iam.infrastructure.remote.IamApiService
import pe.edu.upc.routeguard.features.iam.infrastructure.remote.SignInRequestDto
import pe.edu.upc.routeguard.features.iam.infrastructure.remote.SignUpRequestDto
import pe.edu.upc.routeguard.features.stakeholder.infrastructure.remote.StakeholderApiService
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val service: IamApiService,
    private val stakeholderService: StakeholderApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<Account> =
        safeApiCall { service.signIn(SignInRequestDto(email, password)) }
            .mapCatching { dto -> buildAccount(dto).also(::persist) }

    override suspend fun registerAdministrator(
        firstName: String,
        lastName: String,
        organizationName: String,
        email: String,
        password: String
    ): Result<Account> {
        val organization = safeApiCall { service.createOrganization(CreateOrganizationRequestDto(organizationName)) }
            .getOrElse { return Result.failure(it) }

        safeApiCall {
            service.signUp(
                SignUpRequestDto(firstName, lastName, email, password, ROLE_ADMIN, organization.id)
            )
        }.onFailure { return Result.failure(it) }

        return signIn(email, password)
    }

    override fun currentAccount(): Account? {
        val session = sessionManager.current() ?: return null
        return Account(
            id = session.userId,
            fullName = session.fullName,
            email = session.email,
            role = Role.from(session.role),
            token = SessionToken(session.token),
            organizationId = session.organizationId,
            profileId = session.profileId
        )
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
        if (role != Role.ADMINISTRATOR && profileId == null) {
            error("Tu cuenta no tiene un perfil de ${if (role == Role.DRIVER) "conductor" else "padre"} asociado")
        }
        return Account(
            id = dto.id,
            fullName = "${dto.firstName} ${dto.lastName}".trim(),
            email = dto.email,
            role = role,
            token = SessionToken(dto.token),
            organizationId = dto.organizationId.orEmpty(),
            profileId = profileId
        )
    }

    private fun persist(account: Account) {
        sessionManager.save(
            UserSession(
                userId = account.id,
                fullName = account.fullName,
                email = account.email,
                role = account.role.name,
                token = account.token.value,
                organizationId = account.organizationId,
                profileId = account.profileId
            )
        )
    }

    private companion object {
        const val ROLE_ADMIN = "ADMIN"
    }
}
