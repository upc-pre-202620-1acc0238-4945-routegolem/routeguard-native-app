package pe.edu.upc.routeguard.trip.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import pe.edu.upc.routeguard.notifications.presentation.navigation.AlertsRoute
import pe.edu.upc.routeguard.trip.presentation.route.RouteMapScreen

@Serializable
data object TripNavGraphRoute

@Serializable
data object RouteMapRoute

fun NavGraphBuilder.tripNavGraph(navController: NavController) {

    navigation<TripNavGraphRoute>(startDestination = RouteMapRoute) {
        composable<RouteMapRoute> {
            RouteMapScreen(onOpenAlerts = { navController.navigate(AlertsRoute) })
        }
    }
}
