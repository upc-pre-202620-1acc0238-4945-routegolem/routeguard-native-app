package pe.edu.upc.routeguard.iam.presentation.register

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
import pe.edu.upc.routeguard.iam.domain.Account

@Composable
fun RegisterAdministratorScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterAdministratorViewModel = hiltViewModel(),
    onRegistered: (Account) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(state.account) {
        state.account?.let(onRegistered)
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = "Registrar administrador", onBack = onBack)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormField(state.organizationName, viewModel::onOrganizationChange, "Empresa de transporte")
            FormField(state.firstName, viewModel::onFirstNameChange, "Nombres")
            FormField(state.lastName, viewModel::onLastNameChange, "Apellidos")
            FormField(state.email, viewModel::onEmailChange, "Correo electrónico", keyboardType = KeyboardType.Email)
            FormField(state.password, viewModel::onPasswordChange, "Contraseña (mín. 8)", isPassword = true)
            ErrorText(state.errorMessage)
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                BigButton(text = "Crear cuenta", onClick = viewModel::register)
            }
        }
    }
}
