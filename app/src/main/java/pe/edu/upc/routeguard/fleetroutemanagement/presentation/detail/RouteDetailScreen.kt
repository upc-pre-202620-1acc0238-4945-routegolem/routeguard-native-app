package pe.edu.upc.routeguard.fleetroutemanagement.presentation.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.FormField
import pe.edu.upc.routeguard.core.designsystem.components.InfoCard
import pe.edu.upc.routeguard.core.designsystem.components.InfoText
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.SecondaryButton
import pe.edu.upc.routeguard.core.designsystem.components.SectionTitle
import pe.edu.upc.routeguard.core.designsystem.components.SelectableRow
import pe.edu.upc.routeguard.core.designsystem.components.map.MapStop
import pe.edu.upc.routeguard.core.designsystem.components.map.RouteGuardMap
import pe.edu.upc.routeguard.fleetroutemanagement.domain.RouteStatus
import pe.edu.upc.routeguard.fleetroutemanagement.domain.ServiceDay

@Composable
fun RouteDetailScreen(
    routeId: String,
    modifier: Modifier = Modifier,
    viewModel: RouteDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(routeId) { viewModel.load(routeId) }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = state.route?.name ?: stringResource(R.string.route_detail_name_loading), onBack = onBack)

        val route = state.route
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.isLoading) CircularProgressIndicator()
            InfoText(state.message)
            ErrorText(state.errorMessage)

            if (route != null) {
                val editable = route.isEditable && !state.isLoading
                Text(
                    text = when (route.status) {
                        RouteStatus.DRAFT -> stringResource(R.string.route_detail_status_draft)
                        RouteStatus.ACTIVE -> stringResource(R.string.route_detail_status_active)
                        RouteStatus.INACTIVE -> stringResource(R.string.route_detail_status_inactive)
                    } + stringResource(R.string.route_detail_stops_suffix, route.stopCount)
                )

                // Pick Waypoints
                SectionTitle(stringResource(R.string.route_detail_stops_heading))
                route.waypoints.forEach { Text("${it.orderIndex}. ${it.name}") }
                RouteGuardMap(
                    stops = route.waypoints.map { MapStop(it.name, it.coordinates, it.orderIndex) },
                    height = 260.dp,
                    routePath = state.roadPath,
                    pickedPoint = if (route.isEditable) state.pickedPoint else null,
                    onMapClick = if (route.isEditable) viewModel::onMapPicked else null,
                    fitKey = route.waypoints.size to state.mapFocusKey
                )

                if (route.isEditable) {
                    Text(stringResource(R.string.route_detail_tap_map_hint))
                    FormField(state.searchQuery, viewModel::onSearchQueryChange, stringResource(R.string.route_detail_search_address))
                    SecondaryButton(
                        text = if (state.isSearching) stringResource(R.string.route_detail_searching) else stringResource(R.string.route_detail_search_btn),
                        onClick = viewModel::onSearch,
                        enabled = !state.isSearching && state.searchQuery.isNotBlank()
                    )
                    state.searchResults.forEach { place ->
                        InfoCard(
                            title = place.name.ifBlank { place.address },
                            subtitle = place.address,
                            onClick = { viewModel.onPlaceSelected(place) }
                        )
                    }

                    FormField(state.waypointName, viewModel::onWaypointNameChange, stringResource(R.string.route_detail_stop_name))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormField(
                            state.latitude, viewModel::onLatitudeChange, stringResource(R.string.route_detail_lat),
                            modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal
                        )
                        FormField(
                            state.longitude, viewModel::onLongitudeChange, stringResource(R.string.route_detail_lng),
                            modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal
                        )
                    }
                    SecondaryButton(stringResource(R.string.route_detail_add_stop), viewModel::addWaypoint, enabled = editable)

                    // US-04: best order of the stops according to Mapbox
                    if (route.waypoints.size >= 3) {
                        SecondaryButton(stringResource(R.string.route_detail_suggest_order), viewModel::suggestOrder, enabled = editable)
                    }
                    state.suggestedOrder?.let { proposal ->
                        InfoCard(
                            title = stringResource(R.string.route_detail_suggested_title),
                            subtitle = stringResource(R.string.route_detail_suggested_banner)
                        ) {
                            proposal.forEachIndexed { index, stop -> Text("${index + 1}. ${stop.name}") }
                            BigButton(stringResource(R.string.route_detail_apply_order), viewModel::applySuggestedOrder, enabled = editable)
                            SecondaryButton(stringResource(R.string.route_detail_discard_order), viewModel::discardSuggestedOrder)
                        }
                    }
                }

                // Select Vehicle (+ driver)
                SectionTitle(stringResource(R.string.route_detail_vehicle_heading))
                if (route.isEditable) {
                    state.vehicles.forEach { vehicle ->
                        SelectableRow(
                            title = vehicle.plate.value,
                            subtitle = stringResource(R.string.route_detail_seats_fmt, vehicle.model, vehicle.capacity),
                            selected = vehicle.id == state.selectedVehicleId,
                            onClick = { viewModel.onVehicleSelected(vehicle.id) }
                        )
                    }
                } else {
                    Text(route.vehicle?.let { "${it.plate.value} · ${it.model}" } ?: stringResource(R.string.route_detail_no_vehicle))
                }
                SectionTitle(stringResource(R.string.route_detail_driver_label))
                if (route.isEditable) {
                    state.drivers.forEach { driver ->
                        SelectableRow(
                            title = driver.fullName,
                            subtitle = stringResource(R.string.route_detail_license_fmt, driver.licenseNumber.value),
                            selected = driver.id.value == state.selectedDriverId?.value,
                            onClick = { viewModel.onDriverSelected(pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.DriverId(driver.id.value)) }
                        )
                    }
                    SecondaryButton(stringResource(R.string.route_detail_save_vehicle_driver), viewModel::saveVehicle, enabled = editable)
                } else {
                    Text(
                        state.drivers.firstOrNull { it.id.value == route.driverId?.value }?.fullName ?: stringResource(R.string.route_detail_no_driver)
                    )
                }

                // Assign Students To Route (needs the driver first)
                SectionTitle(stringResource(R.string.route_detail_students_heading))
                if (route.isEditable) {
                    if (route.driverId == null || route.driverId.value.isBlank()) {
                        Text(stringResource(R.string.route_detail_assign_driver_first))
                    }
                    if (state.assignableStudents.isEmpty()) {
                        Text(stringResource(R.string.route_detail_no_groups_hint))
                    }
                    state.assignableStudents.forEach { child ->
                        SelectableRow(
                            title = child.fullName,
                            subtitle = stringResource(R.string.group_editor_age_subtitle, child.age),
                            selected = state.selectedStudentIds.any { it.value == child.id.value },
                            onClick = { viewModel.toggleStudent(pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.StudentId(child.id.value)) }
                        )
                    }
                    SecondaryButton(
                        stringResource(R.string.route_detail_save_students),
                        viewModel::saveStudents,
                        enabled = editable && state.selectedStudentIds.isNotEmpty()
                    )
                } else {
                    Text(stringResource(R.string.stakeholders_group_students_count, route.studentIds.size))
                }

                // Define Service Days
                SectionTitle(stringResource(R.string.route_detail_days_section))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceDay.entries.forEach { day ->
                        FilterChip(
                            selected = day in state.selectedDays,
                            onClick = { if (route.isEditable) viewModel.toggleDay(day) },
                            label = { Text(stringResource(day.shortRes)) }
                        )
                    }
                }
                if (route.isEditable) {
                    SecondaryButton(stringResource(R.string.route_detail_save_days), viewModel::saveServiceDays, enabled = editable)
                }

                // Set Departure Time
                SectionTitle(stringResource(R.string.route_detail_departure_section))
                if (route.isEditable) {
                    FormField(state.departureTime, viewModel::onDepartureTimeChange, stringResource(R.string.route_detail_departure_label))
                    SecondaryButton(stringResource(R.string.route_detail_save_departure), viewModel::saveDepartureTime, enabled = editable)
                } else {
                    Text(route.departureTime ?: stringResource(R.string.route_detail_no_time))
                }

                // Route Activation Finalized
                SectionTitle(stringResource(R.string.route_detail_activation_heading))
                BigButton(
                    text = if (route.isEditable) stringResource(R.string.route_detail_activate_btn) else stringResource(R.string.route_detail_active_btn),
                    onClick = viewModel::activate,
                    enabled = editable && route.missingForActivation.isEmpty()
                )
                if (route.isEditable && route.missingActivationRes.isNotEmpty()) {
                    val missingText = route.missingActivationRes.map { stringResource(it) }.joinToString(", ")
                    Text(stringResource(R.string.route_detail_missing_prefix, missingText))
                }
            }
        }
    }
}
