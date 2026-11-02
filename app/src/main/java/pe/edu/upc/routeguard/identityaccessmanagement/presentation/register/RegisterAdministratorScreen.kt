package pe.edu.upc.routeguard.identityaccessmanagement.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.R
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
    var showTermsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.account) {
        state.account?.let(onRegistered)
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.terms_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.terms_dialog_content),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text(stringResource(R.string.btn_accept))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        BrandHeader(
            title = stringResource(R.string.btn_register_admin),
            subtitle = stringResource(R.string.label_org_name),
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
            FormField(
                value = state.organizationName,
                onValueChange = viewModel::onOrganizationChange,
                label = stringResource(R.string.label_org_name)
            )
            FormField(
                value = state.firstName,
                onValueChange = viewModel::onFirstNameChange,
                label = stringResource(R.string.label_first_name)
            )
            FormField(
                value = state.lastName,
                onValueChange = viewModel::onLastNameChange,
                label = stringResource(R.string.label_last_name)
            )
            FormField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = stringResource(R.string.label_email),
                keyboardType = KeyboardType.Email
            )
            FormField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = stringResource(R.string.label_password_hint),
                isPassword = true
            )

            // Terms and Conditions checkbox row (Problema #1 de RuwaLabs Heuristics)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.acceptedTerms,
                    onCheckedChange = viewModel::onAcceptedTermsChange
                )
                Row(
                    modifier = Modifier.padding(start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.terms_accept_prefix),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.terms_link),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { showTermsDialog = true }
                    )
                }
            }

            ErrorText(state.errorMessage)

            Spacer(modifier = Modifier.height(4.dp))

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                BigButton(
                    text = stringResource(R.string.btn_create_account),
                    onClick = viewModel::register,
                    enabled = state.acceptedTerms
                )
            }
        }
    }
}
