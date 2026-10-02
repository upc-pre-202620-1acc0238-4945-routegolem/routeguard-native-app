package pe.edu.upc.routeguard.features.fleet.presentation.routes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.FormField
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.SecondaryButton
import pe.edu.upc.routeguard.features.fleet.domain.RouteStatus

@Composable
fun RoutesScreen(
    modifier: Modifier = Modifier,
    viewModel: RoutesViewModel = hiltViewModel(),
    onRouteClick: (String) -> Unit = {}
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(state.createdRouteId) {
        state.createdRouteId?.let {
            viewModel.onNavigatedToCreated()
            onRouteClick(it)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = "Rutas")

        if (state.isLoading && state.routes.isEmpty()) {
            LoadingBox()
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormField(state.newRouteName, viewModel::onNewRouteNameChange, "Nombre de la nueva ruta")
                    SecondaryButton("Definir ruta", viewModel::createRoute, enabled = !state.isLoading)
                    ErrorText(state.errorMessage)
                }
            }
            items(state.routes, key = { it.id }) { route ->
                InfoCard(
                    title = route.name,
                    subtitle = when (route.status) {
                        RouteStatus.DRAFT -> "Borrador"
                        RouteStatus.ACTIVE -> "Activa"
                        RouteStatus.INACTIVE -> "Inactiva"
                    } + " · ${route.stopCount} paradas · ${route.studentIds.size} estudiantes" +
                        (route.departureTime?.let { " · sale $it" } ?: ""),
                    onClick = { onRouteClick(route.id) }
                )
            }
        }
    }
}
