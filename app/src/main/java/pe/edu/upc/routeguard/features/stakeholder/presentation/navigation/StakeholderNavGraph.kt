package pe.edu.upc.routeguard.features.stakeholder.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import pe.edu.upc.routeguard.features.stakeholder.presentation.group.GroupEditorScreen
import pe.edu.upc.routeguard.features.stakeholder.presentation.home.StakeholdersScreen
import pe.edu.upc.routeguard.features.stakeholder.presentation.registration.RegisterChildScreen
import pe.edu.upc.routeguard.features.stakeholder.presentation.registration.RegisterDriverScreen
import pe.edu.upc.routeguard.features.stakeholder.presentation.registration.RegisterParentScreen

@Serializable
data object StakeholderNavGraphRoute

@Serializable
data object StakeholdersRoute

@Serializable
data object RegisterDriverRoute

@Serializable
data object RegisterParentRoute

@Serializable
data object RegisterChildRoute

@Serializable
data object GroupEditorRoute

fun NavGraphBuilder.stakeholderNavGraph(navController: NavController) {

    navigation<StakeholderNavGraphRoute>(startDestination = StakeholdersRoute) {
        composable<StakeholdersRoute> {
            StakeholdersScreen(
                onRegisterDriver = { navController.navigate(RegisterDriverRoute) },
                onRegisterParent = { navController.navigate(RegisterParentRoute) },
                onRegisterChild = { navController.navigate(RegisterChildRoute) },
                onCreateGroup = { navController.navigate(GroupEditorRoute) }
            )
        }
        composable<RegisterDriverRoute> {
            RegisterDriverScreen(onBack = { navController.popBackStack() })
        }
        composable<RegisterParentRoute> {
            RegisterParentScreen(onBack = { navController.popBackStack() })
        }
        composable<RegisterChildRoute> {
            RegisterChildScreen(onBack = { navController.popBackStack() })
        }
        composable<GroupEditorRoute> {
            GroupEditorScreen(onBack = { navController.popBackStack() })
        }
    }
}
