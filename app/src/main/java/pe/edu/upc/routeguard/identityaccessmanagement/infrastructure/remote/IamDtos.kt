package pe.edu.upc.routeguard.identityaccessmanagement.infrastructure.remote

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
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["organizationId", "organization_id"])
    val id: String,
    val name: String,
    val status: String?
)

data class UserDto(
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["userId", "user_id"])
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val roleTier: String,
    val organizationId: String?
)

data class AuthenticatedUserDto(
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["authenticatedUserId", "authenticatedUser_id", "userId", "user_id"])
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val roleTier: String,
    val organizationId: String?,
    val token: String
)
