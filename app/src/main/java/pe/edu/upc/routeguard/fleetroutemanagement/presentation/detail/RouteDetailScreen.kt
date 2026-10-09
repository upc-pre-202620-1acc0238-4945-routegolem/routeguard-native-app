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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        ScreenHeader(title = state.route?.name ?: "Ruta", onBack = onBack)

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
                        RouteStatus.DRAFT -> "Ruta en borrador"
                        RouteStatus.ACTIVE -> "Ruta activa (ya no se puede modificar)"
                        RouteStatus.INACTIVE -> "Ruta inactiva (ya no se puede modificar)"
                    } + " · ${route.stopCount} paradas"
                )

                // Pick Waypoints
                SectionTitle("Paradas")
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
                    Text("Busca una dirección o toca el mapa para elegir la ubicación de la nueva parada.")
                    FormField(state.searchQuery, viewModel::onSearchQueryChange, "Buscar dirección (ej. Av. Larco 345)")
                    SecondaryButton(
                        text = if (state.isSearching) "Buscando..." else "Buscar",
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

                    FormField(state.waypointName, viewModel::onWaypointNameChange, "Nombre de la parada")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormField(
                            state.latitude, viewModel::onLatitudeChange, "Latitud",
                            modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal
                        )
                        FormField(
                            state.longitude, viewModel::onLongitudeChange, "Longitud",
                            modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal
                        )
                    }
                    SecondaryButton("Agregar parada", viewModel::addWaypoint, enabled = editable)

                    // US-04: best order of the stops according to Mapbox
                    if (route.waypoints.size >= 3) {
                        SecondaryButton("Sugerir mejor orden de paradas", viewModel::suggestOrder, enabled = editable)
                    }
                    state.suggestedOrder?.let { proposal ->
                        InfoCard(
                            title = "Orden sugerido",
                            subtitle = "La primera y la última parada se mantienen."
                        ) {
                            proposal.forEachIndexed { index, stop -> Text("${index + 1}. ${stop.name}") }
                            BigButton("Aplicar este orden", viewModel::applySuggestedOrder, enabled = editable)
                            SecondaryButton("Descartar", viewModel::discardSuggestedOrder)
                        }
                    }
                }

                // Select Vehicle (+ driver)
                SectionTitle("Vehículo")
                if (route.isEditable) {
                    state.vehicles.forEach { vehicle ->
                        SelectableRow(
                            title = vehicle.plate.value,
                            subtitle = "${vehicle.model} · ${vehicle.capacity} asientos",
                            selected = vehicle.id == state.selectedVehicleId,
                            onClick = { viewModel.onVehicleSelected(vehicle.id) }
                        )
                    }
                } else {
                    Text(route.vehicle?.let { "${it.plate.value} · ${it.model}" } ?: "Sin vehículo")
                }
                SectionTitle("Conductor")
                if (route.isEditable) {
                    state.drivers.forEach { driver ->
                        SelectableRow(
                            title = driver.fullName,
                            subtitle = "Licencia ${driver.licenseNumber.value}",
                            selected = driver.id.value == state.selectedDriverId?.value,
                            onClick = { viewModel.onDriverSelected(pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.DriverId(driver.id.value)) }
                        )
                    }
                    SecondaryButton("Asignar vehículo y conductor", viewModel::saveVehicle, enabled = editable)
                } else {
                    Text(
                        state.drivers.firstOrNull { it.id.value == route.driverId?.value }?.fullName ?: "Sin conductor"
                    )
                }

                // Assign Students To Route (needs the driver first)
                SectionTitle("Estudiantes (de grupos finalizados)")
                if (route.isEditable) {
                    if (route.driverId == null || route.driverId.value.isBlank()) {
                        Text("Asigna primero un conductor para poder asignar estudiantes.")
                    }
                    if (state.assignableStudents.isEmpty()) {
                        Text("Aún no hay grupos finalizados con estudiantes.")
                    }
                    state.assignableStudents.forEach { child ->
                        SelectableRow(
                            title = child.fullName,
                            subtitle = "${child.age} años",
                            selected = state.selectedStudentIds.any { it.value == child.id.value },
                            onClick = { viewModel.toggleStudent(pe.edu.upc.routeguard.fleetroutemanagement.domain.valueobject.StudentId(child.id.value)) }
                        )
                    }
                    SecondaryButton(
                        "Asignar estudiantes",
                        viewModel::saveStudents,
                        enabled = editable && state.selectedStudentIds.isNotEmpty()
                    )
                } else {
                    Text("${route.studentIds.size} estudiantes asignados")
                }

                // Define Service Days
                SectionTitle("Días de servicio")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceDay.entries.forEach { day ->
                        FilterChip(
                            selected = day in state.selectedDays,
                            onClick = { if (route.isEditable) viewModel.toggleDay(day) },
                            label = { Text(day.label) }
                        )
                    }
                }
                if (route.isEditable) {
                    SecondaryButton("Guardar días", viewModel::saveServiceDays, enabled = editable)
                }

                // Set Departure Time
                SectionTitle("Hora de salida")
                if (route.isEditable) {
                    FormField(state.departureTime, viewModel::onDepartureTimeChange, "HH:mm (ej. 06:30)")
                    SecondaryButton("Guardar hora", viewModel::saveDepartureTime, enabled = editable)
                } else {
                    Text(route.departureTime ?: "Sin hora")
                }

                // Route Activation Finalized
                SectionTitle("Activación")
                BigButton(
                    text = if (route.isEditable) "Activar ruta" else "Ruta activa",
                    onClick = viewModel::activate,
                    enabled = editable && route.missingForActivation.isEmpty()
                )
                if (route.isEditable && route.missingForActivation.isNotEmpty()) {
                    Text("Falta: ${route.missingForActivation.joinToString(", ")}.")
                }
            }
        }
    }
}
