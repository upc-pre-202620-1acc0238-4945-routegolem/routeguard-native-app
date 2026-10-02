package pe.edu.upc.routeguard.features.fleet.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.routeguard.features.fleet.presentation.detail.RouteDetailScreen
import pe.edu.upc.routeguard.features.fleet.presentation.routes.RoutesScreen

@Serializable
data object FleetNavGraphRoute

@Serializable
data object RoutesRoute

@Serializable
data class RouteDetailRoute(val routeId: String)

fun NavGraphBuilder.fleetNavGraph(navController: NavController) {

    navigation<FleetNavGraphRoute>(startDestination = RoutesRoute) {
        composable<RoutesRoute> {
            RoutesScreen(onRouteClick = { navController.navigate(RouteDetailRoute(it)) })
        }
        composable<RouteDetailRoute> { backStackEntry ->
            val route: RouteDetailRoute = backStackEntry.toRoute()
            RouteDetailScreen(
                routeId = route.routeId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
