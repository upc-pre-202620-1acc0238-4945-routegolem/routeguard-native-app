package pe.edu.upc.routeguard.features.stakeholder.application

import pe.edu.upc.routeguard.features.stakeholder.domain.Group
import pe.edu.upc.routeguard.features.stakeholder.domain.GroupRepository
import javax.inject.Inject

class GetGroupsUseCase @Inject constructor(private val repository: GroupRepository) {

    suspend operator fun invoke() = repository.getGroups()
}

class CreateGroupUseCase @Inject constructor(private val repository: GroupRepository) {

    suspend operator fun invoke(name: String): Result<Group> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("El grupo necesita un nombre"))
        }
        return repository.createGroup(name.trim())
    }
}

/** Includes students (already linked to a parent) in the group, one by one. */
class IncludeLinkedStudentsUseCase @Inject constructor(private val repository: GroupRepository) {

    suspend operator fun invoke(group: Group, studentIds: Collection<String>): Result<Group> {
        val pending = studentIds.filter { it !in group.studentIds }
        if (pending.isEmpty()) {
            return Result.failure(IllegalArgumentException("Selecciona al menos un estudiante nuevo"))
        }
        var current = group
        for (studentId in pending) {
            current = repository.includeStudent(group.id, studentId).getOrElse { return Result.failure(it) }
        }
        return Result.success(current)
    }
}

/** A group is not finalized without at least one student included. */
class FinalizeGroupUseCase @Inject constructor(private val repository: GroupRepository) {

    suspend operator fun invoke(group: Group): Result<Group> {
        if (group.studentIds.isEmpty()) {
            return Result.failure(IllegalStateException("El grupo necesita al menos un estudiante incluido"))
        }
        return repository.finalizeGroup(group.id)
    }
}
