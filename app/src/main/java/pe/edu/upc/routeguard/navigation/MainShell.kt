package pe.edu.upc.routeguard.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.assistant.AssistantGuideOverlay
import pe.edu.upc.routeguard.core.designsystem.components.assistant.AssistantTutorialProvider
import pe.edu.upc.routeguard.core.designsystem.theme.BrandBorder
import pe.edu.upc.routeguard.core.designsystem.theme.BrandCardWhite
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy
import pe.edu.upc.routeguard.fleetroutemanagement.presentation.navigation.FleetNavGraphRoute
import pe.edu.upc.routeguard.fleetroutemanagement.presentation.navigation.fleetNavGraph
import pe.edu.upc.routeguard.identityaccessmanagement.domain.Role
import pe.edu.upc.routeguard.identityaccessmanagement.presentation.navigation.ProfileNavGraphRoute
import pe.edu.upc.routeguard.identityaccessmanagement.presentation.navigation.profileNavGraph
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
        ShellTab(R.string.tab_plans, Icons.Filled.Star, SubscriptionNavGraphRoute),
        ShellTab(R.string.tab_profile, Icons.Filled.AccountCircle, ProfileNavGraphRoute)
    )

    Role.DRIVER -> listOf(
        ShellTab(R.string.tab_trip, Icons.Filled.LocationOn, TripNavGraphRoute),
        ShellTab(R.string.tab_alerts, Icons.Filled.Notifications, NotificationsNavGraphRoute),
        ShellTab(R.string.tab_profile, Icons.Filled.AccountCircle, ProfileNavGraphRoute)
    )

    Role.PARENT -> listOf(
        ShellTab(R.string.tab_tracking, Icons.Filled.LocationOn, FollowTripNavGraphRoute),
        ShellTab(R.string.tab_alerts, Icons.Filled.Notifications, NotificationsNavGraphRoute),
        ShellTab(R.string.tab_profile, Icons.Filled.AccountCircle, ProfileNavGraphRoute)
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

    // Interactive Mascot Guide (Poké Boy style coach marks)
    val tutorialSteps = remember(role) { AssistantTutorialProvider.stepsFor(role) }
    var showTutorial by rememberSaveable { mutableStateOf(false) }
    var tutorialStepIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(account.id.value) {
        if (!viewModel.hasSeenTutorial(account.id.value)) {
            tutorialStepIndex = 0
            showTutorial = true
        }
    }

    fun navigateToTutorialStep(newIndex: Int) {
        tutorialStepIndex = newIndex
        val targetTab = tutorialSteps.getOrNull(newIndex)?.targetTabIndex
        if (targetTab != null && targetTab in tabs.indices) {
            selectedTab = targetTab
            navController.navigate(tabs[targetTab].route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

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

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
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

                    profileNavGraph(
                        navController = navController,
                        account = account,
                        languageFlow = viewModel.languageFlow,
                        onLanguageSelected = viewModel::setLanguage,
                        onSignOut = { showSignOutDialog = true },
                        onLaunchTutorial = {
                            navigateToTutorialStep(0)
                            showTutorial = true
                        }
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BrandCardWhite,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, BrandBorder)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.widthIn(max = if (tabs.size <= 3) 420.dp else 560.dp)
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                val labelText = stringResource(tab.labelRes)
                                val isSelected = selectedTab == index && !showTutorial
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        selectedTab = index
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = labelText
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = labelText,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = BrandNavy.copy(alpha = 0.12f),
                                        selectedIconColor = BrandNavy,
                                        selectedTextColor = BrandNavy,
                                        unselectedIconColor = Color(0xFF64748B),
                                        unselectedTextColor = Color(0xFF64748B)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Mascot Interactive Tutorial Layer
        if (showTutorial) {
            AssistantGuideOverlay(
                steps = tutorialSteps,
                currentStepIndex = tutorialStepIndex,
                onNext = {
                    if (tutorialStepIndex < tutorialSteps.size - 1) {
                        navigateToTutorialStep(tutorialStepIndex + 1)
                    } else {
                        viewModel.markTutorialAsSeen(account.id.value)
                        showTutorial = false
                    }
                },
                onPrevious = {
                    if (tutorialStepIndex > 0) {
                        navigateToTutorialStep(tutorialStepIndex - 1)
                    }
                },
                onDismiss = {
                    viewModel.markTutorialAsSeen(account.id.value)
                    showTutorial = false
                }
            )
        }
    }
}
