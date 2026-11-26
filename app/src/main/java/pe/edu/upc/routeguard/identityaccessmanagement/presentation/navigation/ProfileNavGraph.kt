package pe.edu.upc.routeguard.identityaccessmanagement.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.coroutines.flow.StateFlow
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
    languageFlow: StateFlow<String>,
    onLanguageSelected: (String) -> Unit,
    onSignOut: () -> Unit,
    onLaunchTutorial: () -> Unit
) {
    navigation<ProfileNavGraphRoute>(startDestination = ProfileRoute) {
        composable<ProfileRoute> {
            val selectedLanguage by languageFlow.collectAsStateWithLifecycle()
            ProfileScreen(
                account = account,
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected,
                onSignOut = onSignOut,
                onLaunchTutorial = onLaunchTutorial
            )
        }
    }
}
