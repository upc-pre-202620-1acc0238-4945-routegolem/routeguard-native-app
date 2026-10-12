package pe.edu.upc.routeguard.stakeholderassetmanagement.presentation.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.SectionTitle
import pe.edu.upc.routeguard.core.designsystem.components.SelectableRow

@Composable
fun RegisterChildScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterChildViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(state.registered) {
        if (state.registered) onBack()
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = "Registrar estudiante", onBack = onBack)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormField(state.firstName, viewModel::onFirstNameChange, "Nombres")
            FormField(state.lastName, viewModel::onLastNameChange, "Apellidos")
            FormField(state.age, viewModel::onAgeChange, "Edad", keyboardType = KeyboardType.Number)

            SectionTitle("Vincular a un padre registrado")
            state.parents.forEach { parent ->
                SelectableRow(
                    title = parent.fullName,
                    subtitle = parent.email.value,
                    selected = state.selectedParentId == parent.id,
                    onClick = { viewModel.onParentSelected(parent.id) }
                )
            }

            ErrorText(state.errorMessage)
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                BigButton(text = "Registrar estudiante", onClick = viewModel::register)
            }
        }
    }
}
