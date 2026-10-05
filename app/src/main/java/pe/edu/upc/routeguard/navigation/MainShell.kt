package pe.edu.upc.routeguard.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import pe.edu.upc.routeguard.fleet.presentation.navigation.FleetNavGraphRoute
import pe.edu.upc.routeguard.fleet.presentation.navigation.fleetNavGraph
import pe.edu.upc.routeguard.iam.domain.Role
import pe.edu.upc.routeguard.notifications.presentation.navigation.NotificationsNavGraphRoute
import pe.edu.upc.routeguard.notifications.presentation.navigation.notificationsNavGraph
import pe.edu.upc.routeguard.stakeholder.presentation.navigation.StakeholderNavGraphRoute
import pe.edu.upc.routeguard.stakeholder.presentation.navigation.stakeholderNavGraph
import pe.edu.upc.routeguard.subscription.presentation.navigation.SubscriptionNavGraphRoute
import pe.edu.upc.routeguard.subscription.presentation.navigation.subscriptionNavGraph
import pe.edu.upc.routeguard.trip.presentation.navigation.FollowTripNavGraphRoute
import pe.edu.upc.routeguard.trip.presentation.navigation.LiveMonitorNavGraphRoute
import pe.edu.upc.routeguard.trip.presentation.navigation.TripNavGraphRoute
import pe.edu.upc.routeguard.trip.presentation.navigation.followTripNavGraph
import pe.edu.upc.routeguard.trip.presentation.navigation.liveMonitorNavGraph
import pe.edu.upc.routeguard.trip.presentation.navigation.tripNavGraph

@Serializable
data object MainShellRoute

private data class ShellTab(val label: String, val icon: ImageVector, val route: Any)

private fun tabsFor(role: Role): List<ShellTab> = when (role) {
    Role.ADMINISTRATOR -> listOf(
        ShellTab("Personas", Icons.Filled.Person, StakeholderNavGraphRoute),
        ShellTab("Rutas", Icons.Filled.Place, FleetNavGraphRoute),
        ShellTab("En vivo", Icons.Filled.PlayArrow, LiveMonitorNavGraphRoute),
        ShellTab("Planes", Icons.Filled.Star, SubscriptionNavGraphRoute)
    )

    Role.DRIVER -> listOf(
        ShellTab("Viaje", Icons.Filled.LocationOn, TripNavGraphRoute),
        ShellTab("Alertas", Icons.Filled.Notifications, NotificationsNavGraphRoute)
    )

    Role.PARENT -> listOf(
        ShellTab("Seguimiento", Icons.Filled.LocationOn, FollowTripNavGraphRoute),
        ShellTab("Alertas", Icons.Filled.Notifications, NotificationsNavGraphRoute)
    )
}

/** Role based shell: each profile only sees the features that belong to it. */
@Composable
fun MainShell(
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val account = viewModel.account
    if (account == null) {
        LaunchedEffect(Unit) { onSignedOut() }
        return
    }

    val role = account.role
    val tabs = tabsFor(role)
    val navController = rememberNavController()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val notificationsPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = tabs.first().route
            ) {
                when (role) {
                    Role.ADMINISTRATOR -> {
                        stakeholderNavGraph(navController)
                        fleetNavGraph(navController)
                        liveMonitorNavGraph(navController)
                        subscriptionNavGraph(navController)
                    }

                    Role.DRIVER -> {
                        tripNavGraph(navController)
                        notificationsNavGraph(navController, isDriver = true)
                    }

                    Role.PARENT -> {
                        followTripNavGraph(navController)
                        notificationsNavGraph(navController, isDriver = false)
                    }
                }
            }
        }

        NavigationBar {
            tabs.forEachIndexed { index, tab ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) }
                )
            }
            NavigationBarItem(
                selected = false,
                onClick = {
                    viewModel.onSignOut()
                    onSignedOut()
                },
                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir") },
                label = { Text("Salir") }
            )
        }
    }
}
