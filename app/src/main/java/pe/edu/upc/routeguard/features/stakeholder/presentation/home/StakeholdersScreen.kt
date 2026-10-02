package pe.edu.upc.routeguard.features.stakeholder.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.SecondaryButton

@Composable
fun StakeholdersScreen(
    modifier: Modifier = Modifier,
    viewModel: StakeholdersViewModel = hiltViewModel(),
    onRegisterDriver: () -> Unit = {},
    onRegisterParent: () -> Unit = {},
    onRegisterChild: () -> Unit = {},
    onCreateGroup: () -> Unit = {}
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = "Conductores, padres y grupos")

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StakeholderTab.entries.forEach { tab ->
                FilterChip(
                    selected = state.tab == tab,
                    onClick = { viewModel.onTabSelected(tab) },
                    label = { Text(tab.label) }
                )
            }
        }

        ErrorText(state.errorMessage, Modifier.padding(16.dp))

        if (state.isLoading && state.drivers.isEmpty() && state.parents.isEmpty() && state.groups.isEmpty()) {
            LoadingBox()
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (state.tab) {
                StakeholderTab.DRIVERS -> {
                    item { SecondaryButton("Registrar conductor", onRegisterDriver) }
                    items(state.drivers, key = { it.id }) { driver ->
                        InfoCard(
                            title = driver.fullName,
                            subtitle = "Licencia ${driver.licenseNumber} · ${driver.phone}\n${driver.email}"
                        )
                    }
                }

                StakeholderTab.PARENTS -> {
                    item { SecondaryButton("Registrar padre", onRegisterParent) }
                    item { SecondaryButton("Registrar estudiante", onRegisterChild) }
                    items(state.parents, key = { it.id }) { parent ->
                        InfoCard(
                            title = parent.fullName,
                            subtitle = "${parent.email} · ${parent.phone}"
                        ) {
                            parent.children.forEach { Text(text = "• ${it.fullName} (${it.age} años)") }
                        }
                    }
                }

                StakeholderTab.GROUPS -> {
                    item { SecondaryButton("Crear grupo", onCreateGroup) }
                    items(state.groups, key = { it.id }) { group ->
                        InfoCard(
                            title = group.name,
                            subtitle = (if (group.isFinalized) "Finalizado" else "Borrador") +
                                " · ${group.studentIds.size} estudiantes"
                        )
                    }
                }
            }
        }
    }
}
