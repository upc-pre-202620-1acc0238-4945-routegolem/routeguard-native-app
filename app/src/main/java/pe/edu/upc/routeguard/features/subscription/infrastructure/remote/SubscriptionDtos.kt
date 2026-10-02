package pe.edu.upc.routeguard.features.subscription.infrastructure.remote

data class PlanDto(
    val id: String,
    val planTier: String,
    val maxRoutes: Int,
    val maxDrivers: Int,
    val price: Double
)

data class SubscriptionDto(
    val id: String,
    val organizationId: String,
    val planId: String,
    val state: String,
    val startDate: String?,
    val endDate: String?,
    val remainingDays: Int
)

data class CreateSubscriptionRequestDto(
    val organizationId: String,
    val planId: String,
    val startDate: String,
    val endDate: String
)

data class UpgradeSubscriptionRequestDto(val planId: String)
