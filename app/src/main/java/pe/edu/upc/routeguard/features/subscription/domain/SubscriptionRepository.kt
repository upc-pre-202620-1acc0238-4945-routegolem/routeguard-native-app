package pe.edu.upc.routeguard.features.subscription.domain

interface SubscriptionRepository {
    suspend fun getPlans(): Result<List<Plan>>

    /** Null when the organization has no active subscription yet. */
    suspend fun getCurrentSubscription(): Result<AccountSubscription?>

    /** Subscribes to a plan for [days] days; the subscription starts active once paid. */
    suspend fun subscribe(planId: String, days: Int): Result<AccountSubscription>

    suspend fun upgradePlan(subscriptionId: String, planId: String): Result<AccountSubscription>
}
