package pe.edu.upc.routeguard.notificationscommunication.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import pe.edu.upc.routeguard.notificationscommunication.presentation.alerts.AlertsScreen

@Serializable
data object NotificationsNavGraphRoute

@Serializable
data object AlertsRoute

/** [isDriver] enables the panic button and the announcements to the parents. */
fun NavGraphBuilder.notificationsNavGraph(navController: NavController, isDriver: Boolean) {

    navigation<NotificationsNavGraphRoute>(startDestination = AlertsRoute) {
        composable<AlertsRoute> {
            AlertsScreen(isDriver = isDriver)
        }
    }
}
