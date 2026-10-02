package pe.edu.upc.routeguard.features.subscription.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.ApiDates
import pe.edu.upc.routeguard.core.network.safeApiCall
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.features.subscription.domain.AccountSubscription
import pe.edu.upc.routeguard.features.subscription.domain.Plan
import pe.edu.upc.routeguard.features.subscription.domain.PlanTier
import pe.edu.upc.routeguard.features.subscription.domain.Quota
import pe.edu.upc.routeguard.features.subscription.domain.SubscriptionRepository
import pe.edu.upc.routeguard.features.subscription.domain.SubscriptionStatus
import pe.edu.upc.routeguard.features.subscription.infrastructure.remote.CreateSubscriptionRequestDto
import pe.edu.upc.routeguard.features.subscription.infrastructure.remote.PlanDto
import pe.edu.upc.routeguard.features.subscription.infrastructure.remote.SubscriptionApiService
import pe.edu.upc.routeguard.features.subscription.infrastructure.remote.SubscriptionDto
import pe.edu.upc.routeguard.features.subscription.infrastructure.remote.UpgradeSubscriptionRequestDto
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

    override suspend fun subscribe(planId: String, days: Int): Result<AccountSubscription> {
        val start = System.currentTimeMillis()
        val end = start + days * DAY_MILLIS
        return safeApiCall {
            service.createSubscription(
                CreateSubscriptionRequestDto(
                    organizationId = sessionManager.organizationId(),
                    planId = planId,
                    startDate = ApiDates.format(start),
                    endDate = ApiDates.format(end)
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun upgradePlan(subscriptionId: String, planId: String): Result<AccountSubscription> =
        safeApiCall { service.upgradePlan(subscriptionId, UpgradeSubscriptionRequestDto(planId)) }
            .map { it.toDomain() }

    private fun PlanDto.toDomain() = Plan(
        id = id,
        tier = PlanTier.from(planTier),
        price = price,
        quota = Quota(maxRoutes = maxRoutes, maxDrivers = maxDrivers)
    )

    private fun SubscriptionDto.toDomain() = AccountSubscription(
        id = id,
        planId = planId,
        status = SubscriptionStatus.from(state),
        remainingDays = remainingDays
    )

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
