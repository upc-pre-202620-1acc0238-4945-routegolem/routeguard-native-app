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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import pe.edu.upc.routeguard.core.designsystem.components.SectionTitle
import pe.edu.upc.routeguard.core.designsystem.components.SelectableRow

@Composable
fun RegisterChildScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterChildViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    var parentSearchQuery by rememberSaveable { mutableStateOf("") }
    val filteredParents = remember(state.parents, parentSearchQuery) {
        if (parentSearchQuery.isBlank()) state.parents
        else state.parents.filter {
            it.fullName.contains(parentSearchQuery, ignoreCase = true) ||
            it.email.value.contains(parentSearchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(state.registered) {
        if (state.registered) onBack()
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.register_child_title), onBack = onBack)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormField(state.firstName, viewModel::onFirstNameChange, stringResource(R.string.label_first_name))
            FormField(state.lastName, viewModel::onLastNameChange, stringResource(R.string.label_last_name))
            FormField(state.age, viewModel::onAgeChange, stringResource(R.string.register_child_age_label), keyboardType = KeyboardType.Number)

            SectionTitle(stringResource(R.string.register_child_link_parent))
            FormField(
                value = parentSearchQuery,
                onValueChange = { parentSearchQuery = it },
                label = stringResource(R.string.register_child_search_parent)
            )
            filteredParents.forEach { parent ->
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
                BigButton(text = stringResource(R.string.register_child_title), onClick = viewModel::register)
            }
        }
    }
}
