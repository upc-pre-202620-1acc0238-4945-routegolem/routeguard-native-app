package pe.edu.upc.routeguard.stakeholderassetmanagement.presentation.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
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
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader

@Composable
fun RegisterDriverScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterDriverViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.register_driver_title), onBack = onBack)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormField(state.firstName, viewModel::onFirstNameChange, stringResource(R.string.label_first_name))
            FormField(state.lastName, viewModel::onLastNameChange, stringResource(R.string.label_last_name))
            FormField(state.phone, viewModel::onPhoneChange, stringResource(R.string.label_phone), keyboardType = KeyboardType.Phone)
            FormField(state.licenseNumber, viewModel::onLicenseChange, stringResource(R.string.label_license))
            FormField(state.email, viewModel::onEmailChange, stringResource(R.string.label_email), keyboardType = KeyboardType.Email)
            ErrorText(state.errorMessage)

            val credentials = state.credentials
            if (credentials != null) {
                CredentialsCard(credentials)
                BigButton(text = stringResource(R.string.btn_done), onClick = onBack)
            } else if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                BigButton(text = stringResource(R.string.register_driver_btn), onClick = viewModel::register)
            }
        }
    }
}
