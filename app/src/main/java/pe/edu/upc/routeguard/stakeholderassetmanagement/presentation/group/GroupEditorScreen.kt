package pe.edu.upc.routeguard.stakeholderassetmanagement.presentation.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.core.designsystem.components.BigButton
import pe.edu.upc.routeguard.core.designsystem.components.ErrorText
import pe.edu.upc.routeguard.core.designsystem.components.FormField
import pe.edu.upc.routeguard.core.designsystem.components.ScreenHeader
import pe.edu.upc.routeguard.core.designsystem.components.SecondaryButton
import pe.edu.upc.routeguard.core.designsystem.components.SectionTitle
import pe.edu.upc.routeguard.core.designsystem.components.SelectableRow
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.StudentId

/** Create Group -> pick parents -> Include Linked Students -> Finalize Group. */
@Composable
fun GroupEditorScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupEditorViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(state.finalized) {
        if (state.finalized) onBack()
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.group_editor_title), onBack = onBack)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val group = state.group

            SectionTitle(stringResource(R.string.group_editor_step1))
            FormField(state.name, viewModel::onNameChange, stringResource(R.string.group_editor_name_label))
            if (group == null) {
                BigButton(text = stringResource(R.string.stakeholders_btn_create_group), onClick = viewModel::create, enabled = !state.isLoading)
            } else {
                Text(text = stringResource(R.string.group_editor_created_info, group.name, group.studentIds.size))

                SectionTitle(stringResource(R.string.group_editor_step2))
                state.parents.forEach { parent ->
                    SelectableRow(
                        title = parent.fullName,
                        subtitle = stringResource(R.string.stakeholders_group_students_count, parent.children.size),
                        selected = parent.id in state.selectedParentIds,
                        onClick = { viewModel.toggleParent(parent.id) }
                    )
                }

                SectionTitle(stringResource(R.string.group_editor_step3))
                if (state.availableStudents.isEmpty()) {
                    Text(stringResource(R.string.group_editor_step3_empty))
                }
                state.availableStudents.forEach { child ->
                    SelectableRow(
                        title = child.fullName,
                        subtitle = stringResource(R.string.group_editor_age_subtitle, child.age),
                        selected = StudentId(child.id.value) in state.selectedStudentIds,
                        onClick = { viewModel.toggleStudent(StudentId(child.id.value)) }
                    )
                }
                SecondaryButton(
                    text = stringResource(R.string.group_editor_include_btn),
                    onClick = viewModel::includeSelectedStudents,
                    enabled = state.selectedStudentIds.isNotEmpty() && !state.isLoading
                )

                SectionTitle(stringResource(R.string.group_editor_step4))
                BigButton(
                    text = stringResource(R.string.group_editor_finalize_btn),
                    onClick = viewModel::finalizeCurrent,
                    enabled = group.studentIds.isNotEmpty() && !state.isLoading
                )
            }

            ErrorText(state.errorMessage)
            if (state.isLoading) CircularProgressIndicator()
        }
    }
}
