package pe.edu.upc.routeguard.subscriptionplanmanagement.presentation.plans

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import pe.edu.upc.routeguard.core.designsystem.components.SectionTitle
import pe.edu.upc.routeguard.core.designsystem.components.StatusChip
import pe.edu.upc.routeguard.core.designsystem.theme.BrandBorder
import pe.edu.upc.routeguard.core.designsystem.theme.BrandCardWhite
import pe.edu.upc.routeguard.core.designsystem.theme.BrandEmeraldDark
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy
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
        ScreenHeader(title = "Planes de Suscripción")

        if (state.isLoading && state.plans.isEmpty()) {
            LoadingBox()
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InfoText(state.message)
                ErrorText(state.errorMessage)
            }

            item {
                val current = state.current
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (current == null) "Estado de Suscripción" else "Plan ${state.currentPlan?.tier?.label ?: ""}".trim(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy
                            )
                            StatusChip(
                                text = if (current == null) "Sin plan activo" else "Activo",
                                containerColor = if (current == null) Color(0xFFFEF3C7) else Color(0xFFD1FAE5),
                                contentColor = if (current == null) Color(0xFF92400E) else Color(0xFF064E3B)
                            )
                        }
                        Text(
                            text = if (current == null) {
                                "Selecciona uno de los planes institucionales para habilitar el seguimiento GPS y gestión de rutas escolares."
                            } else {
                                "Tu suscripción se encuentra activa. Quedan ${current.remainingDays} días de vigencia en el periodo actual."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            item {
                SectionTitle(text = "Planes Disponibles")
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
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        Text(
                            text = "Pagos procesados de forma segura con Stripe Checkout (Modo Sandbox).",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 3.dp else 1.dp),
        border = BorderStroke(
            if (isCurrent) 2.dp else 1.dp,
            if (isCurrent) BrandNavy else BrandBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = plan.tier.label,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                    Text(
                        text = "S/ ${"%.2f".format(plan.price)} / mes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
                if (isCurrent) {
                    StatusChip(
                        text = "Plan Actual",
                        containerColor = Color(0xFFD1FAE5),
                        contentColor = Color(0xFF064E3B)
                    )
                }
            }

            // Benefits list
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PlanFeatureItem("Hasta ${plan.quota.maxRoutes} rutas escolares concurrentes")
                PlanFeatureItem("Hasta ${plan.quota.maxDrivers} conductores registrados")
                PlanFeatureItem("Monitoreo telemétrico GPS en tiempo real")
                PlanFeatureItem("App para padres con red de cuidadores autorizados")
            }

            Spacer(modifier = Modifier.height(4.dp))

            when {
                isCurrent -> {
                    BigButton(
                        text = "Renovar suscripción con Stripe",
                        onClick = onUpgrade,
                        enabled = enabled,
                        containerColor = BrandNavy
                    )
                }
                hasSubscription && isUpgrade -> {
                    BigButton(
                        text = "Mejorar a este plan con Stripe",
                        onClick = onUpgrade,
                        enabled = enabled,
                        containerColor = BrandNavy
                    )
                }
                hasSubscription -> {
                    BigButton(
                        text = "Cambiar a este plan con Stripe",
                        onClick = onUpgrade,
                        enabled = enabled,
                        containerColor = Color(0xFF475569)
                    )
                }
                else -> {
                    BigButton(
                        text = "Elegir y pagar con Stripe",
                        onClick = onSubscribe,
                        enabled = enabled,
                        containerColor = BrandNavy
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanFeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = BrandEmeraldDark,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF334155)
        )
    }
}
