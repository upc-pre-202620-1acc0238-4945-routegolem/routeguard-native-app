package pe.edu.upc.routeguard.subscription.domain

import pe.edu.upc.routeguard.subscription.domain.valueobject.PlanId
import pe.edu.upc.routeguard.subscription.domain.valueobject.SubscriptionId

interface SubscriptionRepository {
    suspend fun getPlans(): Result<List<Plan>>

    /** Null when the organization has no active subscription yet. */
    suspend fun getCurrentSubscription(): Result<AccountSubscription?>

    /** Subscribes to a plan for [days] days; the subscription starts active once paid. */
    suspend fun subscribe(planId: PlanId, days: Int): Result<AccountSubscription>

    suspend fun upgradePlan(subscriptionId: SubscriptionId, planId: PlanId): Result<AccountSubscription>
}
