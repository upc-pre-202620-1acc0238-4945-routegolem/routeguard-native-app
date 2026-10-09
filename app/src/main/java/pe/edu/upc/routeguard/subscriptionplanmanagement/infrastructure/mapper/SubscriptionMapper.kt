package pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.mapper

import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.AccountSubscription
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.Plan
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.PlanTier
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.Quota
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.SubscriptionStatus
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.valueobject.PlanId
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.valueobject.SubscriptionId
import pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.remote.PlanDto
import pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.remote.SubscriptionDto

fun PlanDto.toDomain() = Plan(
    id = PlanId(id),
    tier = PlanTier.from(planTier),
    price = price,
    quota = Quota(maxRoutes = maxRoutes, maxDrivers = maxDrivers)
)

fun SubscriptionDto.toDomain() = AccountSubscription(
    id = SubscriptionId(id),
    planId = PlanId(planId),
    status = SubscriptionStatus.from(state),
    remainingDays = remainingDays
)
