package pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.repositories

import pe.edu.upc.routeguard.core.network.ApiDates
import pe.edu.upc.routeguard.core.network.safeApiCall
import pe.edu.upc.routeguard.core.session.SessionManager
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.AccountSubscription
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.Plan
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.PlanTier
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.Quota
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.SubscriptionRepository
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.SubscriptionStatus
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.valueobject.PlanId
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.valueobject.SubscriptionId
import pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.mapper.toDomain
import pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.remote.CreateSubscriptionRequestDto
import pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.remote.PlanDto
import pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.remote.SubscriptionApiService
import pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.remote.SubscriptionDto
import pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.remote.UpgradeSubscriptionRequestDto
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
