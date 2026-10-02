package pe.edu.upc.routeguard.features.stakeholder.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.safeApiCall
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.features.iam.infrastructure.remote.IamApiService
import pe.edu.upc.routeguard.features.iam.infrastructure.remote.SignUpRequestDto
import pe.edu.upc.routeguard.features.stakeholder.application.TemporaryPasswordGenerator
import pe.edu.upc.routeguard.features.stakeholder.domain.AccountCredentials
import pe.edu.upc.routeguard.features.stakeholder.domain.Child
import pe.edu.upc.routeguard.features.stakeholder.domain.Driver
import pe.edu.upc.routeguard.features.stakeholder.domain.DriverRepository
import pe.edu.upc.routeguard.features.stakeholder.domain.Group
import pe.edu.upc.routeguard.features.stakeholder.domain.GroupRepository
import pe.edu.upc.routeguard.features.stakeholder.domain.Parent
import pe.edu.upc.routeguard.features.stakeholder.domain.ParentRepository
import pe.edu.upc.routeguard.features.stakeholder.domain.Provisioned
import pe.edu.upc.routeguard.features.stakeholder.infrastructure.remote.AddChildRequestDto
import pe.edu.upc.routeguard.features.stakeholder.infrastructure.remote.CreateDriverRequestDto
import pe.edu.upc.routeguard.features.stakeholder.infrastructure.remote.CreateParentRequestDto
import pe.edu.upc.routeguard.features.stakeholder.infrastructure.remote.CreateStudentGroupRequestDto
import pe.edu.upc.routeguard.features.stakeholder.infrastructure.remote.GroupChildRequestDto
import pe.edu.upc.routeguard.features.stakeholder.infrastructure.remote.StakeholderApiService
import javax.inject.Inject

/**
 * Anti-corruption gateway towards IAM: drivers and parents need a user account before their
 * profile exists, and it is created with automatic credentials.
 */
class AccountProvisioningGateway @Inject constructor(
    private val iamService: IamApiService,
    private val sessionManager: SessionManager
) {
    suspend fun provision(
        firstName: String,
        lastName: String,
        email: String,
        roleTier: String
    ): Result<Pair<String, AccountCredentials>> {
        val password = TemporaryPasswordGenerator.generate()
        return safeApiCall {
            iamService.signUp(
                SignUpRequestDto(firstName, lastName, email, password, roleTier, sessionManager.organizationId())
            )
        }.map { user -> user.id to AccountCredentials(user.email, password) }
    }
}

class DriverRepositoryImpl @Inject constructor(
    private val service: StakeholderApiService,
    private val accounts: AccountProvisioningGateway,
    private val sessionManager: SessionManager
) : DriverRepository {

    override suspend fun getDrivers(): Result<List<Driver>> =
        safeApiCall { service.getDrivers() }.map { list -> list.map { it.toDomain() } }

    override suspend fun registerDriver(
        firstName: String,
        lastName: String,
        phone: String,
        licenseNumber: String,
        email: String
    ): Result<Provisioned<Driver>> {
        val (userId, credentials) = accounts.provision(firstName, lastName, email, ROLE_DRIVER)
            .getOrElse { return Result.failure(it) }

        return safeApiCall {
            service.createDriver(
                CreateDriverRequestDto(
                    organizationId = sessionManager.organizationId(),
                    userId = userId,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phoneNumber = phone,
                    licenseNumber = licenseNumber
                )
            )
        }.map { Provisioned(it.toDomain(), credentials) }
    }

    private companion object {
        const val ROLE_DRIVER = "DRIVER"
    }
}

class ParentRepositoryImpl @Inject constructor(
    private val service: StakeholderApiService,
    private val accounts: AccountProvisioningGateway,
    private val sessionManager: SessionManager
) : ParentRepository {

    override suspend fun getParents(): Result<List<Parent>> =
        safeApiCall { service.getParents() }.map { list -> list.map { it.toDomain() } }

    override suspend fun registerParent(
        firstName: String,
        lastName: String,
        phone: String,
        email: String
    ): Result<Provisioned<Parent>> {
        val (userId, credentials) = accounts.provision(firstName, lastName, email, ROLE_PARENT)
            .getOrElse { return Result.failure(it) }

        return safeApiCall {
            service.createParent(
                CreateParentRequestDto(
                    organizationId = sessionManager.organizationId(),
                    userId = userId,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phoneNumber = phone
                )
            )
        }.map { Provisioned(it.toDomain(), credentials) }
    }

    override suspend fun registerChild(
        parentId: String,
        firstName: String,
        lastName: String,
        age: Int
    ): Result<Child> =
        safeApiCall { service.addChild(parentId, AddChildRequestDto(firstName, lastName, age)) }
            .mapCatching { parent ->
                parent.toDomain().children.lastOrNull() ?: error("El estudiante no fue registrado")
            }

    private companion object {
        const val ROLE_PARENT = "PARENT"
    }
}

class GroupRepositoryImpl @Inject constructor(
    private val service: StakeholderApiService,
    private val sessionManager: SessionManager
) : GroupRepository {

    override suspend fun getGroups(): Result<List<Group>> =
        safeApiCall { service.getGroups() }.map { list -> list.map { it.toDomain() } }

    override suspend fun createGroup(name: String): Result<Group> =
        safeApiCall {
            service.createGroup(CreateStudentGroupRequestDto(sessionManager.organizationId(), name))
        }.map { it.toDomain() }

    override suspend fun includeStudent(groupId: String, studentId: String): Result<Group> =
        safeApiCall { service.addChildToGroup(groupId, GroupChildRequestDto(studentId)) }.map { it.toDomain() }

    override suspend fun finalizeGroup(groupId: String): Result<Group> =
        safeApiCall { service.finalizeGroup(groupId) }.map { it.toDomain() }
}
