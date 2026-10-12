package pe.edu.upc.routeguard.subscriptionplanmanagement.domain

import pe.edu.upc.routeguard.shared.domain.AggregateRoot
import pe.edu.upc.routeguard.shared.domain.BaseEntity
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.valueobject.PlanId
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.valueobject.SubscriptionId

enum class PlanTier(val label: String) {
    BASIC("Básico"),
    INTERMEDIATE("Intermedio"),
    COMPLETE("Completo");

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
