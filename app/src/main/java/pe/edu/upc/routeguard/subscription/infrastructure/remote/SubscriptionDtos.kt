package pe.edu.upc.routeguard.subscription.infrastructure.remote

data class PlanDto(
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["planId", "plan_id"])
    val id: String,
    val planTier: String,
    val maxRoutes: Int,
    val maxDrivers: Int,
    val price: Double
)

data class SubscriptionDto(
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["subscriptionId", "subscription_id"])
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
