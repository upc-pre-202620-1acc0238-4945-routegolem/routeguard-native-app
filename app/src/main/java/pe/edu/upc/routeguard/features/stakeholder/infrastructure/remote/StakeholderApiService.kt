package pe.edu.upc.routeguard.features.stakeholder.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StakeholderApiService {

    @GET("drivers")
    suspend fun getDrivers(): Response<List<DriverDto>>

    @POST("drivers")
    suspend fun createDriver(@Body request: CreateDriverRequestDto): Response<DriverDto>

    @GET("parents")
    suspend fun getParents(): Response<List<ParentDto>>

    @POST("parents")
    suspend fun createParent(@Body request: CreateParentRequestDto): Response<ParentDto>

    @POST("parents/{parentId}/children")
    suspend fun addChild(
        @Path("parentId") parentId: String,
        @Body request: AddChildRequestDto
    ): Response<ParentDto>

    @GET("student-groups")
    suspend fun getGroups(): Response<List<StudentGroupDto>>

    @POST("student-groups")
    suspend fun createGroup(@Body request: CreateStudentGroupRequestDto): Response<StudentGroupDto>

    @POST("student-groups/{groupId}/children")
    suspend fun addChildToGroup(
        @Path("groupId") groupId: String,
        @Body request: GroupChildRequestDto
    ): Response<StudentGroupDto>

    @POST("student-groups/{groupId}/finalize")
    suspend fun finalizeGroup(@Path("groupId") groupId: String): Response<StudentGroupDto>
}
