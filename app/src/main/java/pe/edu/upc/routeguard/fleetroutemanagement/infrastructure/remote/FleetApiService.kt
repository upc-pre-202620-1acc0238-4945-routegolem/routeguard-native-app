package pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FleetApiService {

    @GET("routes")
    suspend fun getRoutes(@Query("organizationId") organizationId: String): Response<List<RouteDto>>

    @GET("routes/{routeId}")
    suspend fun getRoute(@Path("routeId") routeId: String): Response<RouteDto>

    @GET("vehicles")
    suspend fun getVehicles(): Response<List<CatalogVehicleDto>>

    @POST("routes")
    suspend fun defineRoute(@Body request: CreateRouteRequestDto): Response<RouteDto>

    @POST("routes/{routeId}/stops")
    suspend fun pickWaypoint(
        @Path("routeId") routeId: String,
        @Body request: AddStopRequestDto
    ): Response<RouteDto>

    @DELETE("routes/{routeId}/stops/{stopId}")
    suspend fun removeWaypoint(
        @Path("routeId") routeId: String,
        @Path("stopId") stopId: String
    ): Response<RouteDto>

    @PUT("routes/{routeId}/driver")
    suspend fun assignDriver(
        @Path("routeId") routeId: String,
        @Body request: AssignDriverRequestDto
    ): Response<RouteDto>

    @POST("routes/{routeId}/children")
    suspend fun assignStudent(
        @Path("routeId") routeId: String,
        @Body request: AssignChildRequestDto
    ): Response<RouteDto>

    @PUT("routes/{routeId}/vehicle")
    suspend fun selectVehicle(
        @Path("routeId") routeId: String,
        @Body request: AssignVehicleRequestDto
    ): Response<RouteDto>

    @PUT("routes/{routeId}/service-days")
    suspend fun defineServiceDays(
        @Path("routeId") routeId: String,
        @Body request: ServiceDaysRequestDto
    ): Response<RouteDto>

    @PUT("routes/{routeId}/departure-time")
    suspend fun setDepartureTime(
        @Path("routeId") routeId: String,
        @Body request: DepartureTimeRequestDto
    ): Response<RouteDto>

    @POST("routes/{routeId}/activate")
    suspend fun activateRoute(@Path("routeId") routeId: String): Response<RouteDto>
}
