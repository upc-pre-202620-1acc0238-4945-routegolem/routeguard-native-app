package pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SubscriptionApiService {

    @GET("plans")
    suspend fun getPlans(): Response<List<PlanDto>>

    @GET("subscriptions")
    suspend fun getSubscriptions(@Query("organizationId") organizationId: String): Response<List<SubscriptionDto>>

    @POST("subscriptions")
    suspend fun createSubscription(@Body request: CreateSubscriptionRequestDto): Response<SubscriptionDto>

    @PUT("subscriptions/{subscriptionId}/plan")
    suspend fun upgradePlan(
        @Path("subscriptionId") subscriptionId: String,
        @Body request: UpgradeSubscriptionRequestDto
    ): Response<SubscriptionDto>
}
