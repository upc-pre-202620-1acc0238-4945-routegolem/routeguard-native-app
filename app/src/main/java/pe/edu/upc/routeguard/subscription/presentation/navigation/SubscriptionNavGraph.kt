package pe.edu.upc.routeguard.subscription.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import pe.edu.upc.routeguard.subscription.presentation.plans.SubscriptionScreen

@Serializable
data object SubscriptionNavGraphRoute

@Serializable
data object PlansRoute

fun NavGraphBuilder.subscriptionNavGraph(navController: NavController) {

    navigation<SubscriptionNavGraphRoute>(startDestination = PlansRoute) {
        composable<PlansRoute> {
            SubscriptionScreen()
        }
    }
}
