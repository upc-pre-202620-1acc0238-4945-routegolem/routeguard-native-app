package pe.edu.upc.routeguard.fleetroutemanagement.presentation.routes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.FormField
import pe.edu.upc.routeguard.core.designsystem.components.LoadingBox
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.SectionTitle
import pe.edu.upc.routeguard.core.designsystem.components.StatusChip
import pe.edu.upc.routeguard.core.designsystem.theme.BrandBorder
import pe.edu.upc.routeguard.core.designsystem.theme.BrandCardWhite
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy
import pe.edu.upc.routeguard.fleetroutemanagement.domain.RouteStatus

@Composable
fun RoutesScreen(
    modifier: Modifier = Modifier,
    viewModel: RoutesViewModel = hiltViewModel(),
    onRouteClick: (String) -> Unit = {}
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(state.createdRouteId) {
        state.createdRouteId?.let {
            viewModel.onNavigatedToCreated()
            onRouteClick(it)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.routes_title))

        if (state.isLoading && state.routes.isEmpty()) {
            LoadingBox()
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.routes_new_section),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy
                        )
                        FormField(
                            value = state.newRouteName,
                            onValueChange = viewModel::onNewRouteNameChange,
                            label = stringResource(R.string.routes_new_placeholder)
                        )
                        BigButton(
                            text = stringResource(R.string.routes_define_btn),
                            onClick = viewModel::createRoute,
                            enabled = !state.isLoading,
                            containerColor = BrandNavy,
                            icon = Icons.Filled.Add
                        )
                    }
                }
            }

            item {
                FormField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = stringResource(R.string.routes_search_placeholder)
                )
                ErrorText(state.errorMessage)
            }

            item {
                SectionTitle(text = stringResource(R.string.routes_list_title, state.routes.size))
            }

            val filteredRoutes = if (searchQuery.isBlank()) state.routes else state.routes.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }

            items(filteredRoutes, key = { it.id.value }) { route ->
                val statusContainer = when (route.status) {
                    RouteStatus.ACTIVE -> Color(0xFFD1FAE5)
                    RouteStatus.DRAFT -> Color(0xFFFEF3C7)
                    RouteStatus.INACTIVE -> Color(0xFFF1F5F9)
                }
                val statusContent = when (route.status) {
                    RouteStatus.ACTIVE -> Color(0xFF064E3B)
                    RouteStatus.DRAFT -> Color(0xFF92400E)
                    RouteStatus.INACTIVE -> Color(0xFF64748B)
                }
                val statusText = when (route.status) {
                    RouteStatus.ACTIVE -> stringResource(R.string.routes_status_active)
                    RouteStatus.DRAFT -> stringResource(R.string.routes_status_draft)
                    RouteStatus.INACTIVE -> stringResource(R.string.routes_status_inactive)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRouteClick(route.id.value) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = BorderStroke(1.dp, BrandBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = route.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy
                                )
                                StatusChip(
                                    text = statusText,
                                    containerColor = statusContainer,
                                    contentColor = statusContent
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.routes_summary_base, route.stopCount, route.studentIds.size) +
                                    (route.departureTime?.let { stringResource(R.string.routes_summary_departure, it) } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.routes_view_details_cd),
                            tint = BrandNavy,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
