package pe.edu.upc.routeguard.identityaccessmanagement.presentation.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.BrandHeader
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.SecondaryButton
import pe.edu.upc.routeguard.core.designsystem.icon.visibility
import pe.edu.upc.routeguard.core.designsystem.icon.visibilityOff
import pe.edu.upc.routeguard.core.designsystem.theme.BrandBorder
import pe.edu.upc.routeguard.core.designsystem.theme.BrandCardWhite
import pe.edu.upc.routeguard.core.designsystem.theme.BrandEmerald
import pe.edu.upc.routeguard.core.designsystem.theme.BrandNavy
import pe.edu.upc.routeguard.identityaccessmanagement.domain.Account

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onAuthenticated: (Account) -> Unit = {},
    onRegisterAdministrator: () -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showRegisterModal by remember { mutableStateOf(false) }

    LaunchedEffect(state.account) {
        state.account?.let(onAuthenticated)
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = BrandNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.forgot_password_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = stringResource(R.string.forgot_password_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text(stringResource(R.string.btn_accept), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            shape = RoundedCornerShape(20.dp),
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text(stringResource(R.string.btn_accept), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showRegisterModal) {
        var regName by remember { mutableStateOf("") }
        var regEmail by remember { mutableStateOf("") }
        var regPassword by remember { mutableStateOf("") }
        var regRole by remember { mutableStateOf("PARENT") }
        var acceptedTerms by remember { mutableStateOf(false) }
        var registerError by remember { mutableStateOf<String?>(null) }
        val errInvalidFields = stringResource(R.string.error_register_invalid_fields)
        val errAcceptTerms = stringResource(R.string.error_register_accept_terms)

        AlertDialog(
            onDismissRequest = { showRegisterModal = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BrandNavy.copy(alpha = 0.1f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = BrandNavy, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.login_register_modal_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (registerError != null) {
                        Text(
                            text = registerError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = regName,
                        onValueChange = { regName = it },
                        label = { Text(stringResource(R.string.label_full_name)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        label = { Text(stringResource(R.string.label_email)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text(stringResource(R.string.label_password_min6)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(stringResource(R.string.label_select_role), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "PARENT" to stringResource(R.string.role_parent_short),
                            "DRIVER" to stringResource(R.string.role_driver_short),
                            "ADMIN" to stringResource(R.string.role_admin_short)
                        ).forEach { (r, label) ->
                            FilterChip(
                                selected = regRole == r,
                                onClick = { regRole = r },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = acceptedTerms,
                            onCheckedChange = { acceptedTerms = it }
                        )
                        Text(
                            text = stringResource(R.string.terms_accept_checkbox),
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandNavy,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { showTermsDialog = true }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (regName.isBlank() || regEmail.isBlank() || !regEmail.contains("@") || regPassword.length < 6) {
                            registerError = errInvalidFields
                            return@TextButton
                        }
                        if (!acceptedTerms) {
                            registerError = errAcceptTerms
                            return@TextButton
                        }
                        viewModel.onEmailChange(regEmail.trim())
                        viewModel.onPasswordChange(regPassword)
                        showRegisterModal = false
                        viewModel.login()
                    }
                ) {
                    Text(stringResource(R.string.btn_register_and_enter), fontWeight = FontWeight.Bold, color = BrandNavy)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterModal = false }) {
                    Text(stringResource(R.string.btn_cancel))
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
            title = stringResource(R.string.app_name),
            subtitle = stringResource(R.string.app_subtitle)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Demo Credentials Selector Card (Paridad exacta con Flutter)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BrandNavy.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, BrandNavy.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.login_quick_demo_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = state.email == "parent@routeguard.pe",
                            onClick = {
                                viewModel.onEmailChange("parent@routeguard.pe")
                                viewModel.onPasswordChange("parent123")
                            },
                            label = { Text(stringResource(R.string.role_parent_short), style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = BrandNavy, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        FilterChip(
                            selected = state.email == "driver@routeguard.pe",
                            onClick = {
                                viewModel.onEmailChange("driver@routeguard.pe")
                                viewModel.onPasswordChange("driver123")
                            },
                            label = { Text(stringResource(R.string.role_driver_short), style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(Icons.Filled.Place, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEF3C7)
                            )
                        )
                        FilterChip(
                            selected = state.email == "admin@routeguard.pe",
                            onClick = {
                                viewModel.onEmailChange("admin@routeguard.pe")
                                viewModel.onPasswordChange("admin123")
                            },
                            label = { Text(stringResource(R.string.role_admin_short), style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE0E7FF)
                            )
                        )
                    }
                }
            }

            // Main Login Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrandCardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, BrandBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.login_heading),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy
                        )
                        Text(
                            text = stringResource(R.string.login_subheading),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text(stringResource(R.string.label_email)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = BrandNavy)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BrandCardWhite,
                            unfocusedContainerColor = BrandCardWhite,
                            focusedBorderColor = BrandNavy,
                            unfocusedBorderColor = BrandBorder,
                            focusedLabelColor = BrandNavy
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text(stringResource(R.string.label_password)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = BrandNavy)
                        },
                        singleLine = true,
                        visualTransformation = if (state.isPasswordHidden) PasswordVisualTransformation()
                        else VisualTransformation.None,
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePasswordVisibility) {
                                Icon(
                                    imageVector = if (state.isPasswordHidden) visibilityOff else visibility,
                                    contentDescription = stringResource(R.string.toggle_password_visibility)
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BrandCardWhite,
                            unfocusedContainerColor = BrandCardWhite,
                            focusedBorderColor = BrandNavy,
                            unfocusedBorderColor = BrandBorder,
                            focusedLabelColor = BrandNavy
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Forgot password recovery link (Problema #2 de RuwaLabs Heuristics)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = stringResource(R.string.label_forgot_password),
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandNavy,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { showForgotPasswordDialog = true }
                        )
                    }

                    ErrorText(state.errorMessage)

                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = BrandNavy,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        BigButton(
                            text = stringResource(R.string.btn_login),
                            onClick = viewModel::login,
                            containerColor = BrandNavy
                        )
                        SecondaryButton(
                            text = stringResource(R.string.btn_register_admin),
                            onClick = onRegisterAdministrator
                        )
                    }

                    // Direct Account Creation Modal Trigger (Heurística Nielsen #3 & Paridad con Flutter)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.login_no_account_prefix),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = stringResource(R.string.btn_create_account),
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandNavy,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showRegisterModal = true }
                        )
                    }
                }
            }
        }
    }
}
