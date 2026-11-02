package pe.edu.upc.routeguard.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.fleetroutemanagement.presentation.navigation.FleetNavGraphRoute
import pe.edu.upc.routeguard.fleetroutemanagement.presentation.navigation.fleetNavGraph
import pe.edu.upc.routeguard.identityaccessmanagement.domain.Role
import pe.edu.upc.routeguard.notificationscommunication.presentation.navigation.NotificationsNavGraphRoute
import pe.edu.upc.routeguard.notificationscommunication.presentation.navigation.notificationsNavGraph
import pe.edu.upc.routeguard.stakeholderassetmanagement.presentation.navigation.StakeholderNavGraphRoute
import pe.edu.upc.routeguard.stakeholderassetmanagement.presentation.navigation.stakeholderNavGraph
import pe.edu.upc.routeguard.subscriptionplanmanagement.presentation.navigation.SubscriptionNavGraphRoute
import pe.edu.upc.routeguard.subscriptionplanmanagement.presentation.navigation.subscriptionNavGraph
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.navigation.FollowTripNavGraphRoute
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.navigation.LiveMonitorNavGraphRoute
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.navigation.TripNavGraphRoute
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.navigation.followTripNavGraph
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.navigation.liveMonitorNavGraph
import pe.edu.upc.routeguard.tripexecutionmonitoring.presentation.navigation.tripNavGraph

@Serializable
data object MainShellRoute

private data class ShellTab(@StringRes val labelRes: Int, val icon: ImageVector, val route: Any)

private fun tabsFor(role: Role): List<ShellTab> = when (role) {
    Role.ADMINISTRATOR -> listOf(
        ShellTab(R.string.tab_people, Icons.Filled.Person, StakeholderNavGraphRoute),
        ShellTab(R.string.tab_routes, Icons.Filled.Place, FleetNavGraphRoute),
        ShellTab(R.string.tab_live, Icons.Filled.PlayArrow, LiveMonitorNavGraphRoute),
        ShellTab(R.string.tab_plans, Icons.Filled.Star, SubscriptionNavGraphRoute)
    )

    Role.DRIVER -> listOf(
        ShellTab(R.string.tab_trip, Icons.Filled.LocationOn, TripNavGraphRoute),
        ShellTab(R.string.tab_alerts, Icons.Filled.Notifications, NotificationsNavGraphRoute)
    )

    Role.PARENT -> listOf(
        ShellTab(R.string.tab_tracking, Icons.Filled.LocationOn, FollowTripNavGraphRoute),
        ShellTab(R.string.tab_alerts, Icons.Filled.Notifications, NotificationsNavGraphRoute)
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
    // The API rejected the token (expired or invalid): back to sign in.
    LaunchedEffect(Unit) { viewModel.sessionRejected.collect { onSignedOut() } }
    if (account == null) {
        LaunchedEffect(Unit) { onSignedOut() }
        return
    }

    val role = account.role
    val tabs = tabsFor(role)
    val navController = rememberNavController()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val notificationsPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.sign_out_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.sign_out_confirm),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.onSignOut()
                        onSignedOut()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.sign_out_action),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
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

        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            tabs.forEachIndexed { index, tab ->
                val labelText = stringResource(tab.labelRes)
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
                    icon = { Icon(tab.icon, contentDescription = labelText) },
                    label = { Text(labelText) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
            val exitText = stringResource(R.string.tab_exit)
            NavigationBarItem(
                selected = false,
                onClick = { showSignOutDialog = true },
                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = exitText) },
                label = { Text(exitText) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
