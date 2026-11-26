package pe.edu.upc.routeguard.core.network

import pe.edu.upc.routeguard.identityaccessmanagement.infrastructure.remote.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class UpdatePhotoRequestDto(val photoData: String?)
data class UpdateLanguageRequestDto(val language: String)
data class ForgotPasswordRequestDto(val email: String)
data class ForgotPasswordResponseDto(
    val sent: Boolean,
    val email: String?,
    val expiresInMinutes: Int?,
    val verificationCode: String?
)
data class ResetPasswordRequestDto(val email: String, val code: String, val newPassword: String)

data class CaregiverDto(
    val id: String,
    val organizationId: String,
    val parentId: String,
    val fullName: String,
    val relationship: String,
    val phoneNumber: String,
    val documentNumber: String?,
    val photoData: String?,
    val createdAt: String
)

data class CreateCaregiverRequestDto(
    val fullName: String,
    val relationship: String,
    val phoneNumber: String,
    val documentNumber: String? = null,
    val photoData: String? = null
)

data class SupportTicketDto(
    val id: String,
    val ticketCode: String,
    val category: String,
    val subject: String,
    val message: String,
    val status: String,
    val createdAt: String
)

data class CreateSupportTicketRequestDto(val category: String, val subject: String, val message: String)

data class RateTripRequestDto(val stars: Int, val comment: String?)
data class TripRatingDto(val id: String, val tripId: String, val stars: Int, val comment: String?, val createdAt: String)

data class AbsenceDto(
    val id: String,
    val childId: String,
    val childName: String,
    val routeId: String,
    val absenceDate: String,
    val reason: String,
    val notes: String?,
    val createdAt: String
)

data class CreateAbsenceRequestDto(
    val childId: String?,
    val childName: String,
    val routeId: String?,
    val absenceDate: String,
    val reason: String,
    val notes: String?
)

data class OptimizedStopDto(
    val stopId: String,
    val originalOrder: Int,
    val reassignedOrder: Int?,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val absentChildName: String?
)

data class OptimizedRouteDto(
    val routeId: String,
    val routeName: String,
    val date: String,
    val totalOriginalStops: Int,
    val activeStopsCount: Int,
    val absentStudents: List<String>?,
    val pendingPickupChanges: Int,
    val stops: List<OptimizedStopDto>?
)

data class VehicleCatalogDto(
    val id: String,
    val organizationId: String,
    val plate: String,
    val brand: String?,
    val model: String,
    val capacity: Int,
    val status: String,
    val soatPolicy: String?,
    val soatInsurer: String?,
    val soatExpiresAt: String?,
    val soatValid: Boolean?
)

data class MaintenanceDto(
    val id: String,
    val vehicleId: String,
    val vehiclePlate: String,
    val maintenanceType: String,
    val description: String,
    val workshop: String,
    val cost: Double,
    val odometerKm: Int,
    val performedAt: String,
    val nextDueDate: String,
    val status: String
)

data class CreateMaintenanceRequestDto(
    val vehicleId: String?,
    val vehiclePlate: String,
    val maintenanceType: String,
    val description: String,
    val workshop: String,
    val cost: Double,
    val odometerKm: Int,
    val performedAt: String?,
    val nextDueDate: String?
)

data class ExpenseDto(
    val id: String,
    val vehicleId: String,
    val vehiclePlate: String,
    val driverName: String,
    val category: String,
    val description: String,
    val amount: Double,
    val expenseDate: String
)

data class CreateExpenseRequestDto(
    val vehicleId: String?,
    val vehiclePlate: String,
    val category: String,
    val description: String,
    val amount: Double,
    val expenseDate: String?
)

data class AnnouncementDto(
    val id: String,
    val routeId: String,
    val authorName: String,
    val title: String,
    val message: String,
    val priority: String,
    val createdAt: String
)

data class CreateAnnouncementRequestDto(
    val routeId: String?,
    val title: String,
    val message: String,
    val priority: String
)

data class PickupChangeDto(
    val id: String,
    val childId: String,
    val childName: String,
    val routeId: String,
    val targetDate: String,
    val newAddress: String,
    val reason: String,
    val status: String,
    val createdAt: String
)

data class CreatePickupChangeRequestDto(
    val childId: String?,
    val childName: String,
    val routeId: String?,
    val targetDate: String,
    val newAddress: String,
    val reason: String
)

data class UpdatePickupChangeStatusDto(val status: String)

data class ChatMessageDto(
    val id: String,
    val routeId: String,
    val senderUserId: String,
    val senderName: String,
    val senderRole: String,
    val message: String,
    val sentAt: String
)

data class SendChatRequestDto(val routeId: String?, val message: String)

data class TripHistoryDto(
    val tripId: String,
    val routeId: String,
    val routeName: String,
    val driverName: String,
    val vehiclePlate: String,
    val status: String,
    val boardedCount: Int,
    val totalStudents: Int,
    val startTime: String?,
    val endTime: String?,
    val myRating: Int?,
    val averageRating: Double?,
    val cancellationReason: String?
)

interface PlatformFeaturesApi {

    @GET("users/me")
    suspend fun getMe(): Response<UserDto>

    @PUT("users/me/photo")
    suspend fun updateMyPhoto(@Body request: UpdatePhotoRequestDto): Response<UserDto>

    @PUT("users/me/language")
    suspend fun updateMyLanguage(@Body request: UpdateLanguageRequestDto): Response<UserDto>

    @POST("users/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): Response<ForgotPasswordResponseDto>

    @POST("users/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): Response<Unit>

    @GET("vehicles")
    suspend fun getVehicles(): Response<List<VehicleCatalogDto>>

    @GET("trips/history")
    suspend fun getTripsHistory(): Response<List<TripHistoryDto>>

    @POST("trips/{tripId}/rating")
    suspend fun rateTrip(@Path("tripId") tripId: String, @Body request: RateTripRequestDto): Response<TripRatingDto>

    @GET("caregivers")
    suspend fun getCaregivers(): Response<List<CaregiverDto>>

    @POST("caregivers")
    suspend fun addCaregiver(@Body request: CreateCaregiverRequestDto): Response<CaregiverDto>

    @DELETE("caregivers/{caregiverId}")
    suspend fun deleteCaregiver(@Path("caregiverId") caregiverId: String): Response<Unit>

    @GET("support-tickets")
    suspend fun getSupportTickets(): Response<List<SupportTicketDto>>

    @POST("support-tickets")
    suspend fun createSupportTicket(@Body request: CreateSupportTicketRequestDto): Response<SupportTicketDto>

    @GET("absences")
    suspend fun getAbsences(@Query("date") date: String? = null): Response<List<AbsenceDto>>

    @POST("absences")
    suspend fun createAbsence(@Body request: CreateAbsenceRequestDto): Response<AbsenceDto>

    @GET("routes/{routeId}/optimized-stops")
    suspend fun getOptimizedStops(
        @Path("routeId") routeId: String,
        @Query("date") date: String? = null
    ): Response<OptimizedRouteDto>

    @GET("maintenances")
    suspend fun getMaintenances(): Response<List<MaintenanceDto>>

    @POST("maintenances")
    suspend fun createMaintenance(@Body request: CreateMaintenanceRequestDto): Response<MaintenanceDto>

    @GET("expenses")
    suspend fun getExpenses(): Response<List<ExpenseDto>>

    @POST("expenses")
    suspend fun createExpense(@Body request: CreateExpenseRequestDto): Response<ExpenseDto>

    @GET("announcements")
    suspend fun getAnnouncements(): Response<List<AnnouncementDto>>

    @POST("announcements")
    suspend fun createAnnouncement(@Body request: CreateAnnouncementRequestDto): Response<AnnouncementDto>

    @GET("pickup-changes")
    suspend fun getPickupChanges(): Response<List<PickupChangeDto>>

    @POST("pickup-changes")
    suspend fun createPickupChange(@Body request: CreatePickupChangeRequestDto): Response<PickupChangeDto>

    @PATCH("pickup-changes/{requestId}/status")
    suspend fun updatePickupChangeStatus(
        @Path("requestId") requestId: String,
        @Body request: UpdatePickupChangeStatusDto
    ): Response<PickupChangeDto>

    @GET("chat")
    suspend fun getChatMessages(@Query("routeId") routeId: String? = null): Response<List<ChatMessageDto>>

    @POST("chat")
    suspend fun sendChatMessage(@Body request: SendChatRequestDto): Response<ChatMessageDto>
}
