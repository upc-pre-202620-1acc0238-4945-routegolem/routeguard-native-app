package pe.edu.upc.routeguard.subscription.infrastructure.mapper

import pe.edu.upc.routeguard.subscription.domain.AccountSubscription
import pe.edu.upc.routeguard.subscription.domain.Plan
import pe.edu.upc.routeguard.subscription.domain.PlanTier
import pe.edu.upc.routeguard.subscription.domain.Quota
import pe.edu.upc.routeguard.subscription.domain.SubscriptionStatus
import pe.edu.upc.routeguard.subscription.domain.valueobject.PlanId
import pe.edu.upc.routeguard.subscription.domain.valueobject.SubscriptionId
import pe.edu.upc.routeguard.subscription.infrastructure.remote.PlanDto
import pe.edu.upc.routeguard.subscription.infrastructure.remote.SubscriptionDto

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
