package pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.follow.FollowTripScreen
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.monitor.LiveMonitorScreen

@Serializable
data object LiveMonitorNavGraphRoute

@Serializable
data object LiveMonitorRoute

/** Administrator: live map of the fleet. */
fun NavGraphBuilder.liveMonitorNavGraph(navController: NavController) {
    navigation<LiveMonitorNavGraphRoute>(startDestination = LiveMonitorRoute) {
        composable<LiveMonitorRoute> {
            LiveMonitorScreen()
        }
    }
}

@Serializable
data object FollowTripNavGraphRoute

@Serializable
data object FollowTripRoute

/** Parent: where is the vehicle of my children. */
fun NavGraphBuilder.followTripNavGraph(navController: NavController) {
    navigation<FollowTripNavGraphRoute>(startDestination = FollowTripRoute) {
        composable<FollowTripRoute> {
            FollowTripScreen()
        }
    }
}
