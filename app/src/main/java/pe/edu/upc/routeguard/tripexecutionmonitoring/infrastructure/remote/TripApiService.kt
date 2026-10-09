package pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TripApiService {

    @GET("routes")
    suspend fun getRoutes(@Query("organizationId") organizationId: String): Response<List<RouteSummaryDto>>

    @GET("routes/{routeId}")
    suspend fun getRoute(@Path("routeId") routeId: String): Response<RouteSummaryDto>

    @GET("parents")
    suspend fun getParents(): Response<List<ParentSummaryDto>>

    @GET("trips/live")
    suspend fun getLiveTrips(): Response<List<LiveTripDto>>

    /** 204 when none of the children of the parent is riding a trip in progress. */
    @GET("parents/{parentId}/active-trip")
    suspend fun getParentActiveTrip(@Path("parentId") parentId: String): Response<LiveTripDto>

    @POST("trips")
    suspend fun createTrip(@Body request: CreateTripRequestDto): Response<TripDto>

    @POST("trips/{tripId}/start")
    suspend fun startTrip(@Path("tripId") tripId: String): Response<TripDto>

    @POST("trips/{tripId}/boarding")
    suspend fun setBoardingStatus(
        @Path("tripId") tripId: String,
        @Body request: BoardingRequestDto
    ): Response<TripDto>

    @POST("trips/{tripId}/incidents")
    suspend fun reportIncident(
        @Path("tripId") tripId: String,
        @Body request: IncidentRequestDto
    ): Response<TripDto>

    @POST("trips/{tripId}/complete")
    suspend fun completeTrip(@Path("tripId") tripId: String): Response<TripDto>

    @POST("trips/{tripId}/locations")
    suspend fun sendLocationUpdate(
        @Path("tripId") tripId: String,
        @Body request: LocationUpdateRequestDto
    ): Response<Unit>

    @POST("trips/{tripId}/offline-sync")
    suspend fun syncOfflineRecords(
        @Path("tripId") tripId: String,
        @Body request: OfflineSyncRequestDto
    ): Response<OfflineSyncResponseDto>
}
