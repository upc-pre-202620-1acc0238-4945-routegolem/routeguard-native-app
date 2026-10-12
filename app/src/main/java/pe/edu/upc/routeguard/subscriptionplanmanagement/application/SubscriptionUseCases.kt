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

class CreatePaymentIntentUseCase @Inject constructor(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(plan: Plan): Result<String> = repository.createPaymentIntent(plan.id)
}

/**
 * Select Plan + Confirm Subscription after Stripe PaymentSheet succeeds.
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
        return repository.upgradePlan(current.id, target.id)
    }
}
