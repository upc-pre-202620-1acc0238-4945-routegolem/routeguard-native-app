package pe.edu.upc.routeguard.subscriptionplanmanagement.domain

import androidx.annotation.StringRes
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.shared.domain.AggregateRoot
import pe.edu.upc.routeguard.shared.domain.BaseEntity
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.valueobject.PlanId
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.valueobject.SubscriptionId

enum class PlanTier(@StringRes val labelRes: Int, val label: String) {
    BASIC(R.string.plan_tier_basic, "Básico"),
    INTERMEDIATE(R.string.plan_tier_intermediate, "Intermedio"),
    COMPLETE(R.string.plan_tier_complete, "Completo");

    companion object {
        fun from(value: String): PlanTier =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: BASIC
    }
}

enum class SubscriptionStatus {
    ACTIVE, EXPIRED, CANCELLED;

    companion object {
        fun from(value: String): SubscriptionStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: EXPIRED
    }
}

/** Maximum number of routes and drivers enabled by a plan. */
data class Quota(
    val maxRoutes: Int,
    val maxDrivers: Int
)

data class Plan(
    override val id: PlanId,
    val tier: PlanTier,
    val price: Double,
    val quota: Quota
) : BaseEntity<PlanId>

/** Current commercial link between the organization and a plan. */
data class AccountSubscription(
    override val id: SubscriptionId,
    val planId: PlanId,
    val status: SubscriptionStatus,
    val remainingDays: Int
) : AggregateRoot<SubscriptionId> {
    val isActive: Boolean get() = status == SubscriptionStatus.ACTIVE
}
