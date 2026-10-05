package pe.edu.upc.routeguard.subscription.presentation.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
import pe.edu.upc.routeguard.core.designsystem.components.InfoText
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.subscription.domain.Plan

@Composable
fun SubscriptionScreen(
    modifier: Modifier = Modifier,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) { viewModel.refresh() }

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
                    text = "El cobro es simulado en modo local: aún no hay pasarela de pago conectada.",
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
            hasSubscription && isUpgrade -> BigButton(text = "Mejorar a este plan", onClick = onUpgrade, enabled = enabled)
            hasSubscription -> Unit
            else -> BigButton(text = "Elegir y pagar", onClick = onSubscribe, enabled = enabled)
        }
    }
}
