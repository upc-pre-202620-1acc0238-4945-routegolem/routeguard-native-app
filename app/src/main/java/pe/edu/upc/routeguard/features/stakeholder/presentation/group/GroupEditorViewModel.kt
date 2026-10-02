package pe.edu.upc.routeguard.features.stakeholder.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.features.stakeholder.application.CreateGroupUseCase
import pe.edu.upc.routeguard.features.stakeholder.application.FinalizeGroupUseCase
import pe.edu.upc.routeguard.features.stakeholder.application.GetParentsUseCase
import pe.edu.upc.routeguard.features.stakeholder.application.IncludeLinkedStudentsUseCase
import pe.edu.upc.routeguard.features.stakeholder.domain.Child
import pe.edu.upc.routeguard.features.stakeholder.domain.Group
import pe.edu.upc.routeguard.features.stakeholder.domain.Parent
import javax.inject.Inject

data class GroupEditorUiState(
    val name: String = "",
    val group: Group? = null,
    val parents: List<Parent> = emptyList(),
    /** Parents picked to narrow down the students that can be included. */
    val selectedParentIds: Set<String> = emptySet(),
    val selectedStudentIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val finalized: Boolean = false,
    val errorMessage: String? = null
) {
    /** Only students linked to the selected parents can be included. */
    val availableStudents: List<Child>
        get() = parents.filter { it.id in selectedParentIds }.flatMap { it.children }
}

@HiltViewModel
class GroupEditorViewModel @Inject constructor(
    private val getParents: GetParentsUseCase,
    private val createGroup: CreateGroupUseCase,
    private val includeStudents: IncludeLinkedStudentsUseCase,
    private val finalizeGroup: FinalizeGroupUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GroupEditorUiState())
    val state: StateFlow<GroupEditorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getParents()
                .onSuccess { list -> _state.update { it.copy(parents = list) } }
                .onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun onNameChange(v: String) = _state.update { it.copy(name = v, errorMessage = null) }

    fun toggleParent(id: String) = _state.update {
        val selected = if (id in it.selectedParentIds) it.selectedParentIds - id else it.selectedParentIds + id
        val validStudents = it.parents.filter { p -> p.id in selected }.flatMap { p -> p.children }.map { c -> c.id }
        it.copy(selectedParentIds = selected, selectedStudentIds = it.selectedStudentIds.intersect(validStudents.toSet()))
    }

    fun toggleStudent(id: String) = _state.update {
        val selected = if (id in it.selectedStudentIds) it.selectedStudentIds - id else it.selectedStudentIds + id
        it.copy(selectedStudentIds = selected)
    }

    fun create() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            createGroup(_state.value.name)
                .onSuccess { group -> _state.update { it.copy(isLoading = false, group = group) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    fun includeSelectedStudents() {
        val s = _state.value
        val group = s.group ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            includeStudents(group, s.selectedStudentIds)
                .onSuccess { updated -> _state.update { it.copy(isLoading = false, group = updated) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }

    fun finalizeCurrent() {
        val group = _state.value.group ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            finalizeGroup(group)
                .onSuccess { updated ->
                    _state.update { it.copy(isLoading = false, group = updated, finalized = true) }
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }
}
