package pe.edu.upc.routeguard.subscriptionplanmanagement.presentation.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.subscriptionplanmanagement.application.CreatePaymentIntentUseCase
import pe.edu.upc.routeguard.subscriptionplanmanagement.application.GetCurrentSubscriptionUseCase
import pe.edu.upc.routeguard.subscriptionplanmanagement.application.GetPlansUseCase
import pe.edu.upc.routeguard.subscriptionplanmanagement.application.SelectPlanUseCase
import pe.edu.upc.routeguard.subscriptionplanmanagement.application.UpgradePlanUseCase
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.AccountSubscription
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.Plan
import javax.inject.Inject

data class PendingStripePayment(
    val clientSecret: String,
    val plan: Plan,
    val isUpgrade: Boolean
)

data class SubscriptionUiState(
    val plans: List<Plan> = emptyList(),
    val current: AccountSubscription? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
    val pendingStripePayment: PendingStripePayment? = null
) {
    val currentPlan: Plan? get() = plans.firstOrNull { it.id == current?.planId }
}

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val getPlans: GetPlansUseCase,
    private val getCurrentSubscription: GetCurrentSubscriptionUseCase,
    private val createPaymentIntent: CreatePaymentIntentUseCase,
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

    /** Initiates Stripe PaymentIntent for subscribing to a plan. */
    fun onSubscribe(plan: Plan) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null, errorMessage = null) }
            createPaymentIntent(plan)
                .onSuccess { clientSecret ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingStripePayment = PendingStripePayment(
                                clientSecret = clientSecret,
                                plan = plan,
                                isUpgrade = false
                            )
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    /** Initiates Stripe PaymentIntent for upgrading to a higher plan. */
    fun onUpgrade(plan: Plan) {
        val state = _uiState.value
        val current = state.current
        if (current == null) {
            _uiState.update { it.copy(errorMessage = "No tienes una suscripción activa") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null, errorMessage = null) }
            createPaymentIntent(plan)
                .onSuccess { clientSecret ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingStripePayment = PendingStripePayment(
                                clientSecret = clientSecret,
                                plan = plan,
                                isUpgrade = true
                            )
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun onPaymentSheetPresented() {
        // Keep pendingStripePayment until result arrives, or we can store activePaymentPlan separately
    }

    fun onPaymentCompleted(pending: PendingStripePayment) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, pendingStripePayment = null, errorMessage = null) }
            val plan = pending.plan
            if (pending.isUpgrade) {
                val current = _uiState.value.current
                if (current == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "No tienes una suscripción activa") }
                    return@launch
                }
                upgradePlan(current, _uiState.value.currentPlan, plan)
                    .onSuccess { subscription ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                current = subscription,
                                message = "Pago con Stripe confirmado: plan mejorado a ${plan.tier.label}"
                            )
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                    }
            } else {
                selectPlan(plan)
                    .onSuccess { subscription ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                current = subscription,
                                message = "Pago con Stripe confirmado: plan ${plan.tier.label} activo"
                            )
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                    }
            }
        }
    }

    fun onPaymentCanceled() {
        _uiState.update {
            it.copy(
                isLoading = false,
                pendingStripePayment = null,
                message = "Proceso de pago cancelado."
            )
        }
    }

    fun onPaymentFailed(error: String?) {
        _uiState.update {
            it.copy(
                isLoading = false,
                pendingStripePayment = null,
                errorMessage = error ?: "Error al procesar el pago con Stripe."
            )
        }
    }
}
