package pe.edu.upc.routeguard.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pe.edu.upc.routeguard.iam.presentation.navigation.IamNavGraphRoute
import pe.edu.upc.routeguard.iam.presentation.navigation.iamNavGraph

/**
 * Root graph: IAM (sign in / register) and the role based [MainShell].
 * [startDestination] is the shell when a session already exists, the IAM graph otherwise.
 */
@Composable
fun AppNavHost(navController: NavHostController, startDestination: Any) {

    // No root-level inset padding: the sign in / register screens draw their brand header behind the
    // status bar, and the main shell applies the safe drawing insets itself.
    Box(modifier = Modifier.fillMaxSize()) {
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
                MainShell(
                    modifier = Modifier.safeDrawingPadding(),
                    onSignedOut = {
                        navController.navigate(IamNavGraphRoute) {
                            popUpTo(MainShellRoute) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
