package pe.edu.upc.routeguard.features.subscription.domain

import pe.edu.upc.routeguard.shared.domain.AggregateRoot
import pe.edu.upc.routeguard.shared.domain.BaseEntity

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
    override val id: String,
    val tier: PlanTier,
    val price: Double,
    val quota: Quota
) : BaseEntity<String>

/** Current commercial link between the organization and a plan. */
data class AccountSubscription(
    override val id: String,
    val planId: String,
    val status: SubscriptionStatus,
    val remainingDays: Int
) : AggregateRoot<String> {
    val isActive: Boolean get() = status == SubscriptionStatus.ACTIVE
}
