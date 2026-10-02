package pe.edu.upc.routeguard.features.iam.infrastructure.remote

data class SignInRequestDto(
    val email: String,
    val password: String
)

data class SignUpRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val roleTier: String,
    val organizationId: String?
)

data class CreateOrganizationRequestDto(val name: String)

data class OrganizationDto(
    val id: String,
    val name: String,
    val status: String?
)

data class UserDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val roleTier: String,
    val organizationId: String?
)

data class AuthenticatedUserDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val roleTier: String,
    val organizationId: String?,
    val token: String
)
