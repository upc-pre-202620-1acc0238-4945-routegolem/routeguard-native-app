package pe.edu.upc.routeguard.iam.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import pe.edu.upc.routeguard.iam.domain.Account
import pe.edu.upc.routeguard.iam.presentation.login.LoginScreen
import pe.edu.upc.routeguard.iam.presentation.register.RegisterAdministratorScreen

@Serializable
data object IamNavGraphRoute

@Serializable
data object LoginRoute

@Serializable
data object RegisterAdministratorRoute

fun NavGraphBuilder.iamNavGraph(
    navController: NavController,
    onAuthenticated: (Account) -> Unit
) {
    navigation<IamNavGraphRoute>(startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(
                onAuthenticated = onAuthenticated,
                onRegisterAdministrator = { navController.navigate(RegisterAdministratorRoute) }
            )
        }
        composable<RegisterAdministratorRoute> {
            RegisterAdministratorScreen(
                onRegistered = onAuthenticated,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
