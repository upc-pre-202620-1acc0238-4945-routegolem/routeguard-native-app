package pe.edu.upc.routeguard.core.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.core.database.AbsenceReportEntity
import pe.edu.upc.routeguard.core.database.AuthorizedCaregiverEntity
import pe.edu.upc.routeguard.core.database.ChatMessageEntity
import pe.edu.upc.routeguard.core.database.OperationalExpenseEntity
import pe.edu.upc.routeguard.core.database.PickupChangeEntity
import pe.edu.upc.routeguard.core.database.RouteAnnouncementEntity
import pe.edu.upc.routeguard.core.database.SupportTicketEntity
import pe.edu.upc.routeguard.core.database.VehicleDocsCacheEntity
import pe.edu.upc.routeguard.core.database.VehicleMaintenanceEntity
import pe.edu.upc.routeguard.core.network.OptimizedRouteDto
import pe.edu.upc.routeguard.core.network.PlatformFeaturesRepository
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class PlatformFeaturesUiState(
    val photoData: String? = null,
    val primaryVehicleDocs: VehicleDocsCacheEntity? = null,
    val caregivers: List<AuthorizedCaregiverEntity> = emptyList(),
    val supportTickets: List<SupportTicketEntity> = emptyList(),
    val absences: List<AbsenceReportEntity> = emptyList(),
    val optimizedRoute: OptimizedRouteDto? = null,
    val maintenances: List<VehicleMaintenanceEntity> = emptyList(),
    val expenses: List<OperationalExpenseEntity> = emptyList(),
    val announcements: List<RouteAnnouncementEntity> = emptyList(),
    val pickupChanges: List<PickupChangeEntity> = emptyList(),
    val chatMessages: List<ChatMessageEntity> = emptyList(),
    val passwordResetHintCode: String? = null,
    val passwordResetSuccess: Boolean = false,
    val isBusy: Boolean = false,
    val feedbackMessage: String? = null
)

@HiltViewModel
class PlatformFeaturesViewModel @Inject constructor(
    private val repo: PlatformFeaturesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlatformFeaturesUiState())
    val uiState: StateFlow<PlatformFeaturesUiState> = _uiState.asStateFlow()

    fun refreshProfileAndDocs() {
        viewModelScope.launch {
            repo.syncAllPending()
            val photo = repo.loadMyPhoto()
            val docs = repo.loadPrimaryVehicleDocs()
            val caregivers = repo.getCaregivers()
            val tickets = repo.getSupportTickets()
            _uiState.update {
                it.copy(
                    photoData = photo,
                    primaryVehicleDocs = docs,
                    caregivers = caregivers,
                    supportTickets = tickets
                )
            }
        }
    }

    fun uploadBitmapPhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            val scaled = Bitmap.createScaledBitmap(bitmap, 256, 256, true)
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val encoded = "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            _uiState.update { it.copy(photoData = encoded) }
            repo.saveMyPhoto(encoded)
        }
    }

    fun addCaregiver(fullName: String, relationship: String, phone: String) {
        viewModelScope.launch {
            repo.addCaregiver(fullName, relationship, phone)
            val updated = repo.getCaregivers()
            _uiState.update { it.copy(caregivers = updated) }
        }
    }

    fun deleteCaregiver(id: String) {
        viewModelScope.launch {
            repo.deleteCaregiver(id)
            val updated = repo.getCaregivers()
            _uiState.update { it.copy(caregivers = updated) }
        }
    }

    fun submitTicket(category: String, subject: String, message: String) {
        viewModelScope.launch {
            repo.createSupportTicket(category, subject, message)
            val updated = repo.getSupportTickets()
            _uiState.update { it.copy(supportTickets = updated) }
        }
    }

    fun refreshOperations(routeId: String = "f0000000-0000-0000-0000-000000000001") {
        viewModelScope.launch {
            repo.syncAllPending()
            val maintenances = repo.getMaintenances()
            val expenses = repo.getExpenses()
            val announcements = repo.getAnnouncements()
            val pickupChanges = repo.getPickupChanges()
            val chat = repo.getChatMessages(routeId)
            val absences = repo.getAbsences()
            val optimized = repo.getOptimizedStops(routeId)
            _uiState.update {
                it.copy(
                    maintenances = maintenances,
                    expenses = expenses,
                    announcements = announcements,
                    pickupChanges = pickupChanges,
                    chatMessages = chat,
                    absences = absences,
                    optimizedRoute = optimized
                )
            }
        }
    }

    fun addMaintenance(plate: String, type: String, description: String, workshop: String, cost: Double, odometerKm: Int) {
        viewModelScope.launch {
            repo.addMaintenance(plate, type, description, workshop, cost, odometerKm)
            val list = repo.getMaintenances()
            _uiState.update { it.copy(maintenances = list) }
        }
    }

    fun addExpense(plate: String, category: String, description: String, amount: Double) {
        viewModelScope.launch {
            repo.addExpense(plate, category, description, amount)
            val list = repo.getExpenses()
            _uiState.update { it.copy(expenses = list) }
        }
    }

    fun publishAnnouncement(routeId: String?, title: String, message: String, priority: String) {
        viewModelScope.launch {
            repo.publishAnnouncement(routeId, title, message, priority)
            val list = repo.getAnnouncements()
            _uiState.update { it.copy(announcements = list) }
        }
    }

    fun requestPickupChange(childName: String, routeId: String?, targetDate: String, newAddress: String, reason: String) {
        viewModelScope.launch {
            repo.requestPickupChange(null, childName, routeId, targetDate, newAddress, reason)
            val list = repo.getPickupChanges()
            _uiState.update { it.copy(pickupChanges = list) }
        }
    }

    fun reviewPickupChange(requestId: String, status: String) {
        viewModelScope.launch {
            repo.updatePickupChangeStatus(requestId, status)
            val list = repo.getPickupChanges()
            _uiState.update { it.copy(pickupChanges = list) }
        }
    }

    fun sendChatMessage(routeId: String?, message: String) {
        viewModelScope.launch {
            repo.sendChatMessage(routeId, message)
            val list = repo.getChatMessages(routeId)
            _uiState.update { it.copy(chatMessages = list) }
        }
    }

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, passwordResetHintCode = null, passwordResetSuccess = false) }
            repo.requestPasswordReset(email)
                .onSuccess { res ->
                    _uiState.update { it.copy(isBusy = false, passwordResetHintCode = res.verificationCode) }
                }
                .onFailure {
                    _uiState.update { it.copy(isBusy = false) }
                }
        }
    }

    fun confirmPasswordReset(email: String, code: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            repo.confirmPasswordReset(email, code, newPassword)
                .onSuccess {
                    _uiState.update { it.copy(isBusy = false, passwordResetSuccess = true) }
                }
                .onFailure {
                    _uiState.update { it.copy(isBusy = false) }
                }
        }
    }

    companion object {
        fun decodeBase64Bitmap(photoData: String?): Bitmap? {
            if (photoData.isNullOrBlank()) return null
            return try {
                val base64 = photoData.substringAfter("base64,", photoData)
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) {
                null
            }
        }
    }
}
