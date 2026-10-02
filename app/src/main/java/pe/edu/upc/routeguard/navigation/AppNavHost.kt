package pe.edu.upc.routeguard.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pe.edu.upc.routeguard.features.iam.presentation.navigation.IamNavGraphRoute
import pe.edu.upc.routeguard.features.iam.presentation.navigation.iamNavGraph

/**
 * Root graph: IAM (sign in / register) and the role based [MainShell].
 * [startDestination] is the shell when a session already exists, the IAM graph otherwise.
 */
@Composable
fun AppNavHost(navController: NavHostController, startDestination: Any) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            iamNavGraph(navController) {
                navController.navigate(MainShellRoute) {
                    popUpTo(IamNavGraphRoute) { inclusive = true }
                }
            }

            composable<MainShellRoute> {
                MainShell(onSignedOut = {
                    navController.navigate(IamNavGraphRoute) {
                        popUpTo(MainShellRoute) { inclusive = true }
                    }
                })
            }
        }
    }
}
