package pe.edu.upc.routeguard.identityaccessmanagement.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.BrandHeader
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.FormField
import pe.edu.upc.routeguard.identityaccessmanagement.domain.Account

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        BrandHeader(
            title = "Registrar administrador",
            subtitle = "Empresa de transporte",
            compact = true,
            onBack = onBack
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormField(state.organizationName, viewModel::onOrganizationChange, "Empresa de transporte")
            FormField(state.firstName, viewModel::onFirstNameChange, "Nombres")
            FormField(state.lastName, viewModel::onLastNameChange, "Apellidos")
            FormField(state.email, viewModel::onEmailChange, "Correo electrónico", keyboardType = KeyboardType.Email)
            FormField(state.password, viewModel::onPasswordChange, "Contraseña (mín. 8)", isPassword = true)
            ErrorText(state.errorMessage)
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                BigButton(text = "Crear cuenta", onClick = viewModel::register)
            }
        }
    }
}
