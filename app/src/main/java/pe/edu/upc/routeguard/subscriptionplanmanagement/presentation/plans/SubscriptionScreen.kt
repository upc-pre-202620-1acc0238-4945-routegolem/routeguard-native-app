package pe.edu.upc.routeguard.subscriptionplanmanagement.presentation.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
import pe.edu.upc.routeguard.core.designsystem.components.InfoText
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.Plan

@Composable
fun SubscriptionScreen(
    modifier: Modifier = Modifier,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    var activePayment by remember { mutableStateOf<PendingStripePayment?>(null) }

    val paymentSheet = rememberPaymentSheet { paymentResult ->
        val pending = activePayment
        activePayment = null
        when (paymentResult) {
            is PaymentSheetResult.Completed -> {
                if (pending != null) {
                    viewModel.onPaymentCompleted(pending)
                }
            }
            is PaymentSheetResult.Canceled -> {
                viewModel.onPaymentCanceled()
            }
            is PaymentSheetResult.Failed -> {
                viewModel.onPaymentFailed(paymentResult.error.localizedMessage)
            }
        }
    }

    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(state.pendingStripePayment) {
        val pending = state.pendingStripePayment
        if (pending != null && activePayment != pending) {
            activePayment = pending
            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret = pending.clientSecret,
                configuration = PaymentSheet.Configuration(
                    merchantDisplayName = "RouteGuard Platform"
                )
            )
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = "Planes y suscripción")

        if (state.isLoading && state.plans.isEmpty()) {
            LoadingBox()
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                InfoText(state.message)
                ErrorText(state.errorMessage)
            }

            item {
                val current = state.current
                InfoCard(
                    title = if (current == null) "Sin suscripción" else "Plan ${state.currentPlan?.tier?.label ?: ""}".trim(),
                    subtitle = if (current == null) {
                        "Elige un plan para habilitar la plataforma."
                    } else {
                        "Suscripción activa · ${current.remainingDays} días restantes"
                    }
                )
            }

            items(state.plans, key = { it.id.value }) { plan ->
                PlanCard(
                    plan = plan,
                    isCurrent = state.current?.planId == plan.id,
                    hasSubscription = state.current != null,
                    isUpgrade = plan.tier.ordinal > (state.currentPlan?.tier?.ordinal ?: -1),
                    enabled = !state.isLoading,
                    onSubscribe = { viewModel.onSubscribe(plan) },
                    onUpgrade = { viewModel.onUpgrade(plan) }
                )
            }

            item {
                Text(
                    text = "Pagos procesados de forma segura con Stripe Checkout (Modo Sandbox).",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: Plan,
    isCurrent: Boolean,
    hasSubscription: Boolean,
    isUpgrade: Boolean,
    enabled: Boolean,
    onSubscribe: () -> Unit,
    onUpgrade: () -> Unit
) {
    InfoCard(
        title = plan.tier.label,
        subtitle = "S/ ${"%.2f".format(plan.price)} / mes"
    ) {
        Text("Hasta ${plan.quota.maxRoutes} rutas y ${plan.quota.maxDrivers} conductores")
        when {
            isCurrent -> Text("Tu plan actual")
            hasSubscription && isUpgrade -> BigButton(text = "Mejorar con Stripe", onClick = onUpgrade, enabled = enabled)
            hasSubscription -> Unit
            else -> BigButton(text = "Elegir y pagar con Stripe", onClick = onSubscribe, enabled = enabled)
        }
    }
}
