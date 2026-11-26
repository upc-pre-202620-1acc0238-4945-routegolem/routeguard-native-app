package pe.edu.upc.routeguard.identityaccessmanagement.infrastructure.mapper

import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.localization.LocalizedException
import pe.edu.upc.routeguard.core.session.UserSession
import pe.edu.upc.routeguard.identityaccessmanagement.domain.Account
import pe.edu.upc.routeguard.identityaccessmanagement.domain.Role
import pe.edu.upc.routeguard.identityaccessmanagement.domain.SessionToken
import pe.edu.upc.routeguard.identityaccessmanagement.domain.valueobject.AccountId
import pe.edu.upc.routeguard.identityaccessmanagement.domain.valueobject.Email
import pe.edu.upc.routeguard.identityaccessmanagement.domain.valueobject.OrganizationId
import pe.edu.upc.routeguard.identityaccessmanagement.domain.valueobject.ProfileId
import pe.edu.upc.routeguard.identityaccessmanagement.infrastructure.remote.AuthenticatedUserDto

fun UserSession.toDomain(): Account = Account(
    id = AccountId(userId),
    fullName = fullName,
    email = Email(email),
    role = Role.from(role),
    token = SessionToken(token),
    organizationId = OrganizationId(organizationId),
    profileId = profileId?.let { ProfileId(it) }
)

fun Account.toSession(): UserSession = UserSession(
    userId = id.value,
    fullName = fullName,
    email = email.value,
    role = role.name,
    token = token.value,
    organizationId = organizationId.value,
    profileId = profileId?.value
)

fun AuthenticatedUserDto.toDomain(profileId: String?): Account {
    val role = Role.from(roleTier)
    if (role != Role.ADMINISTRATOR && profileId == null) {
        if (role == Role.DRIVER) {
            throw LocalizedException(R.string.error_missing_driver_profile, fallback = "Tu cuenta no tiene un perfil de conductor asociado")
        } else {
            throw LocalizedException(R.string.error_missing_parent_profile, fallback = "Tu cuenta no tiene un perfil de padre asociado")
        }
    }
    return Account(
        id = AccountId(id),
        fullName = "$firstName $lastName".trim(),
        email = Email(email),
        role = role,
        token = SessionToken(token),
        organizationId = OrganizationId(organizationId.orEmpty()),
        profileId = profileId?.let { ProfileId(it) }
    )
}
