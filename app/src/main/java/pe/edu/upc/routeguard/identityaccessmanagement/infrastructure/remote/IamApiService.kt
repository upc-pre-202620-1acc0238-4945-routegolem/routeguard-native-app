package pe.edu.upc.routeguard.identityaccessmanagement.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface IamApiService {

    @POST("users/sign-in")
    suspend fun signIn(@Body request: SignInRequestDto): Response<AuthenticatedUserDto>

    @POST("users")
    suspend fun signUp(@Body request: SignUpRequestDto): Response<UserDto>

    @POST("organizations")
    suspend fun createOrganization(@Body request: CreateOrganizationRequestDto): Response<OrganizationDto>
}
