package pe.edu.upc.routeguard.identityaccessmanagement.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.BrandHeader
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.SecondaryButton
import pe.edu.upc.routeguard.core.designsystem.icon.visibility
import pe.edu.upc.routeguard.core.designsystem.icon.visibilityOff
import pe.edu.upc.routeguard.identityaccessmanagement.domain.Account

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onAuthenticated: (Account) -> Unit = {},
    onRegisterAdministrator: () -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(state.account) {
        state.account?.let(onAuthenticated)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        BrandHeader(title = "RouteGuard", subtitle = "Movilidad escolar segura")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Ingresa con tu cuenta para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (state.isPasswordHidden) PasswordVisualTransformation()
                else VisualTransformation.None,
                trailingIcon = {
                    IconButton(onClick = viewModel::togglePasswordVisibility) {
                        Icon(
                            imageVector = if (state.isPasswordHidden) visibilityOff else visibility,
                            contentDescription = "Mostrar u ocultar contraseña"
                        )
                    }
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            )
            ErrorText(state.errorMessage)
            Spacer(modifier = Modifier.height(4.dp))

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                BigButton(text = "Iniciar sesión", onClick = viewModel::login)
                SecondaryButton(text = "Registrar administrador", onClick = onRegisterAdministrator)
            }
        }
    }
}
