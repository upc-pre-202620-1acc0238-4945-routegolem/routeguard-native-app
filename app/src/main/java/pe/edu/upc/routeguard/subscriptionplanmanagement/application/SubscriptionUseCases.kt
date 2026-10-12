package pe.edu.upc.routeguard.subscriptionplanmanagement.application

import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.AccountSubscription
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.Plan
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.SubscriptionRepository
import javax.inject.Inject

class GetPlansUseCase @Inject constructor(private val repository: SubscriptionRepository) {
    suspend operator fun invoke() = repository.getPlans()
}

class GetCurrentSubscriptionUseCase @Inject constructor(private val repository: SubscriptionRepository) {
    suspend operator fun invoke() = repository.getCurrentSubscription()
}

/**
 * Select Plan + Initiate Payment Process. The payment gateway is simulated for now: the
 * subscription is created active for a month (Plan Selected, Payment Confirmed, Subscription Activated).
 */
class SelectPlanUseCase @Inject constructor(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(plan: Plan): Result<AccountSubscription> =
        repository.subscribe(plan.id, SUBSCRIPTION_DAYS)

    private companion object {
        const val SUBSCRIPTION_DAYS = 30
    }
}

/** Plan Upgraded: quotas increase without interrupting the current service. */
class UpgradePlanUseCase @Inject constructor(private val repository: SubscriptionRepository) {

    suspend operator fun invoke(
        current: AccountSubscription,
        currentPlan: Plan?,
        target: Plan
    ): Result<AccountSubscription> {
        if (!current.isActive) {
            return Result.failure(IllegalStateException("Tu suscripción no está activa"))
        }
        if (currentPlan != null && target.tier.ordinal <= currentPlan.tier.ordinal) {
            return Result.failure(IllegalArgumentException("Elige un plan superior al actual"))
        }
        return repository.upgradePlan(current.id, target.id)
    }
}
