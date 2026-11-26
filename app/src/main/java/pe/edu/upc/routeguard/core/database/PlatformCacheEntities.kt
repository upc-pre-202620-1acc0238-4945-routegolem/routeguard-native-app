package pe.edu.upc.routeguard.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_avatar_cache")
data class UserAvatarCacheEntity(
    @PrimaryKey val userId: String,
    val photoData: String?,
    val preferredLanguage: String,
    val pendingSync: Boolean = false
)

@Entity(tableName = "vehicle_docs_cache")
data class VehicleDocsCacheEntity(
    @PrimaryKey val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val capacity: Int,
    val soatPolicy: String,
    val soatInsurer: String,
    val soatExpiresAt: String,
    val soatValid: Boolean
)

@Entity(tableName = "authorized_caregivers")
data class AuthorizedCaregiverEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val parentId: String,
    val fullName: String,
    val relationship: String,
    val phoneNumber: String,
    val documentNumber: String?,
    val photoData: String?,
    val createdAt: String,
    val pendingSync: Boolean = false
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val ticketCode: String,
    val category: String,
    val subject: String,
    val message: String,
    val status: String,
    val createdAt: String,
    val pendingSync: Boolean = false
)

@Entity(tableName = "trip_ratings")
data class TripRatingEntity(
    @PrimaryKey val tripId: String,
    val stars: Int,
    val comment: String,
    val createdAt: String,
    val pendingSync: Boolean = false
)

@Entity(tableName = "absence_reports")
data class AbsenceReportEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val childName: String,
    val routeId: String,
    val absenceDate: String,
    val reason: String,
    val notes: String?,
    val createdAt: String,
    val pendingSync: Boolean = false
)

@Entity(tableName = "vehicle_maintenances")
data class VehicleMaintenanceEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val vehiclePlate: String,
    val maintenanceType: String,
    val description: String,
    val workshop: String,
    val cost: Double,
    val odometerKm: Int,
    val performedAt: String,
    val nextDueDate: String,
    val status: String,
    val pendingSync: Boolean = false
)

@Entity(tableName = "operational_expenses")
data class OperationalExpenseEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val vehiclePlate: String,
    val driverName: String,
    val category: String,
    val description: String,
    val amount: Double,
    val expenseDate: String,
    val pendingSync: Boolean = false
)

@Entity(tableName = "route_announcements")
data class RouteAnnouncementEntity(
    @PrimaryKey val id: String,
    val routeId: String,
    val authorName: String,
    val title: String,
    val message: String,
    val priority: String,
    val createdAt: String,
    val pendingSync: Boolean = false
)

@Entity(tableName = "pickup_change_requests")
data class PickupChangeEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val childName: String,
    val routeId: String,
    val targetDate: String,
    val newAddress: String,
    val reason: String,
    val status: String,
    val createdAt: String,
    val pendingSync: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val routeId: String,
    val senderUserId: String,
    val senderName: String,
    val senderRole: String,
    val message: String,
    @ColumnInfo(name = "sent_at") val sentAt: String,
    val pendingSync: Boolean = false
)
