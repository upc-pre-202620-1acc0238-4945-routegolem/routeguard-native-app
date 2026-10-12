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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        ScreenHeader(title = "Grupo de estudiantes", onBack = onBack)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val group = state.group

            SectionTitle("1. Nombre del grupo")
            FormField(state.name, viewModel::onNameChange, "Nombre")
            if (group == null) {
                BigButton(text = "Crear grupo", onClick = viewModel::create, enabled = !state.isLoading)
            } else {
                Text(text = "Grupo creado: ${group.name} · ${group.studentIds.size} estudiantes incluidos")

                SectionTitle("2. Elige los padres")
                state.parents.forEach { parent ->
                    SelectableRow(
                        title = parent.fullName,
                        subtitle = "${parent.children.size} estudiantes",
                        selected = parent.id in state.selectedParentIds,
                        onClick = { viewModel.toggleParent(parent.id) }
                    )
                }

                SectionTitle("3. Estudiantes vinculados")
                if (state.availableStudents.isEmpty()) {
                    Text("Elige padres que tengan estudiantes registrados.")
                }
                state.availableStudents.forEach { child ->
                    SelectableRow(
                        title = child.fullName,
                        subtitle = "${child.age} años",
                        selected = StudentId(child.id.value) in state.selectedStudentIds,
                        onClick = { viewModel.toggleStudent(StudentId(child.id.value)) }
                    )
                }
                SecondaryButton(
                    text = "Incluir estudiantes",
                    onClick = viewModel::includeSelectedStudents,
                    enabled = state.selectedStudentIds.isNotEmpty() && !state.isLoading
                )

                SectionTitle("4. Finalizar")
                BigButton(
                    text = "Finalizar grupo",
                    onClick = viewModel::finalizeCurrent,
                    enabled = group.studentIds.isNotEmpty() && !state.isLoading
                )
            }

            ErrorText(state.errorMessage)
            if (state.isLoading) CircularProgressIndicator()
        }
    }
}
