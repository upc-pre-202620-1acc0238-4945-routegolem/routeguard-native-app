package pe.edu.upc.routeguard.subscription.presentation.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.subscription.application.GetCurrentSubscriptionUseCase
import pe.edu.upc.routeguard.subscription.application.GetPlansUseCase
import pe.edu.upc.routeguard.subscription.application.SelectPlanUseCase
import pe.edu.upc.routeguard.subscription.application.UpgradePlanUseCase
import pe.edu.upc.routeguard.subscription.domain.AccountSubscription
import pe.edu.upc.routeguard.subscription.domain.Plan
import javax.inject.Inject

data class SubscriptionUiState(
    val plans: List<Plan> = emptyList(),
    val current: AccountSubscription? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
) {
    val currentPlan: Plan? get() = plans.firstOrNull { it.id == current?.planId }
}

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val getPlans: GetPlansUseCase,
    private val getCurrentSubscription: GetCurrentSubscriptionUseCase,
    private val selectPlan: SelectPlanUseCase,
    private val upgradePlan: UpgradePlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val plans = getPlans()
            val current = getCurrentSubscription()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    plans = plans.getOrDefault(state.plans),
                    current = current.getOrDefault(state.current),
                    errorMessage = plans.exceptionOrNull()?.message ?: current.exceptionOrNull()?.message
                )
            }
        }
    }

    /** Select Plan -> Initiate Payment Process (simulated gateway). */
    fun onSubscribe(plan: Plan) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null, errorMessage = null) }
            selectPlan(plan)
                .onSuccess { subscription ->
                    _uiState.update {
                        it.copy(isLoading = false, current = subscription, message = "Pago confirmado: plan ${plan.tier.label} activo")
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    fun onUpgrade(plan: Plan) {
        val state = _uiState.value
        val current = state.current
        if (current == null) {
            _uiState.update { it.copy(errorMessage = "No tienes una suscripción activa") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null, errorMessage = null) }
            upgradePlan(current, state.currentPlan, plan)
                .onSuccess { subscription ->
                    _uiState.update {
                        it.copy(isLoading = false, current = subscription, message = "Plan mejorado a ${plan.tier.label}")
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }
}
