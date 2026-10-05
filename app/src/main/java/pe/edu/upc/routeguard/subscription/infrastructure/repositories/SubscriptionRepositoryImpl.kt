package pe.edu.upc.routeguard.subscription.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.ApiDates
import pe.edu.upc.routeguard.core.network.safeApiCall
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.subscription.domain.AccountSubscription
import pe.edu.upc.routeguard.subscription.domain.Plan
import pe.edu.upc.routeguard.subscription.domain.PlanTier
import pe.edu.upc.routeguard.subscription.domain.Quota
import pe.edu.upc.routeguard.subscription.domain.SubscriptionRepository
import pe.edu.upc.routeguard.subscription.domain.SubscriptionStatus
import pe.edu.upc.routeguard.subscription.domain.valueobject.PlanId
import pe.edu.upc.routeguard.subscription.domain.valueobject.SubscriptionId
import pe.edu.upc.routeguard.subscription.infrastructure.mapper.toDomain
import pe.edu.upc.routeguard.subscription.infrastructure.remote.CreateSubscriptionRequestDto
import pe.edu.upc.routeguard.subscription.infrastructure.remote.PlanDto
import pe.edu.upc.routeguard.subscription.infrastructure.remote.SubscriptionApiService
import pe.edu.upc.routeguard.subscription.infrastructure.remote.SubscriptionDto
import pe.edu.upc.routeguard.subscription.infrastructure.remote.UpgradeSubscriptionRequestDto
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val service: SubscriptionApiService,
    private val sessionManager: SessionManager
) : SubscriptionRepository {

    override suspend fun getPlans(): Result<List<Plan>> =
        safeApiCall { service.getPlans() }
            .map { list -> list.map { it.toDomain() }.sortedBy { it.tier.ordinal } }

    override suspend fun getCurrentSubscription(): Result<AccountSubscription?> =
        safeApiCall { service.getSubscriptions(sessionManager.organizationId()) }.map { list ->
            list.map { it.toDomain() }.firstOrNull { it.isActive }
        }

    override suspend fun subscribe(planId: PlanId, days: Int): Result<AccountSubscription> {
        val start = System.currentTimeMillis()
        val end = start + days * DAY_MILLIS
        return safeApiCall {
            service.createSubscription(
                CreateSubscriptionRequestDto(
                    organizationId = sessionManager.organizationId(),
                    planId = planId.value,
                    startDate = ApiDates.format(start),
                    endDate = ApiDates.format(end)
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun upgradePlan(subscriptionId: SubscriptionId, planId: PlanId): Result<AccountSubscription> =
        safeApiCall { service.upgradePlan(subscriptionId.value, UpgradeSubscriptionRequestDto(planId.value)) }
            .map { it.toDomain() }

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
