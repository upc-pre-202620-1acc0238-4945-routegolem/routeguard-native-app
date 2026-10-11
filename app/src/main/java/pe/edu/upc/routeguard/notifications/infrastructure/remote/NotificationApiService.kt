package pe.edu.upc.routeguard.notifications.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApiService {

    @GET("notifications")
    suspend fun getNotifications(@Query("parentId") parentId: String?): Response<List<NotificationDto>>

    @POST("notifications/{notificationId}/delivered")
    suspend fun markDelivered(@Path("notificationId") notificationId: String): Response<NotificationDto>

    @POST("trips/{tripId}/panic")
    suspend fun triggerPanicAlert(@Path("tripId") tripId: String): Response<FanOutResponseDto>

    @POST("trips/{tripId}/broadcast")
    suspend fun postBroadcastMessage(
        @Path("tripId") tripId: String,
        @Body request: BroadcastRequestDto
    ): Response<FanOutResponseDto>

    @POST("users/{userId}/device-tokens")
    suspend fun registerDeviceToken(
        @Path("userId") userId : String,
        @Body request: RegisterDeviceTokenRequest
    ): Response<Unit>
}
