package pe.edu.upc.routeguard.identityaccessmanagement.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import pe.edu.upc.routeguard.identityaccessmanagement.domain.Account
import pe.edu.upc.routeguard.identityaccessmanagement.presentation.profile.ProfileScreen

@Serializable
data object ProfileNavGraphRoute

@Serializable
data object ProfileRoute

fun NavGraphBuilder.profileNavGraph(
    navController: NavController,
    account: Account,
    onSignOut: () -> Unit,
    onLaunchTutorial: () -> Unit
) {
    navigation<ProfileNavGraphRoute>(startDestination = ProfileRoute) {
        composable<ProfileRoute> {
            ProfileScreen(
                account = account,
                onSignOut = onSignOut,
                onLaunchTutorial = onLaunchTutorial
            )
        }
    }
}
