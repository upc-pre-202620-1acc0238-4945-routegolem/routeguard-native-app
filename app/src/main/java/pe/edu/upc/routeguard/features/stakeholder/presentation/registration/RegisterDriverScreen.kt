package pe.edu.upc.routeguard.features.stakeholder.presentation.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.FormField
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader

@Composable
fun RegisterDriverScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterDriverViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = "Registrar conductor", onBack = onBack)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormField(state.firstName, viewModel::onFirstNameChange, "Nombres")
            FormField(state.lastName, viewModel::onLastNameChange, "Apellidos")
            FormField(state.phone, viewModel::onPhoneChange, "Teléfono", keyboardType = KeyboardType.Phone)
            FormField(state.licenseNumber, viewModel::onLicenseChange, "N.º de licencia")
            FormField(state.email, viewModel::onEmailChange, "Correo electrónico", keyboardType = KeyboardType.Email)
            ErrorText(state.errorMessage)

            val credentials = state.credentials
            if (credentials != null) {
                CredentialsCard(credentials)
                BigButton(text = "Listo", onClick = onBack)
            } else if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                BigButton(text = "Registrar conductor", onClick = viewModel::register)
            }
        }
    }
}
