package pe.edu.upc.routeguard.stakeholder.application

import pe.edu.upc.routeguard.stakeholder.domain.Child
import pe.edu.upc.routeguard.stakeholder.domain.Parent
import pe.edu.upc.routeguard.stakeholder.domain.ParentRepository
import pe.edu.upc.routeguard.stakeholder.domain.Provisioned
import pe.edu.upc.routeguard.stakeholder.domain.valueobject.Email
import pe.edu.upc.routeguard.stakeholder.domain.valueobject.ParentId
import pe.edu.upc.routeguard.stakeholder.domain.valueobject.Phone
import javax.inject.Inject

class GetParentsUseCase @Inject constructor(private val repository: ParentRepository) {

    suspend operator fun invoke() = repository.getParents()
}

/** Students are owned by their parent: they are read from the registered parents. */
class GetChildrenUseCase @Inject constructor(private val repository: ParentRepository) {

    suspend operator fun invoke(): Result<List<Child>> =
        repository.getParents().map { parents -> parents.flatMap { it.children } }
}

class RegisterParentUseCase @Inject constructor(private val repository: ParentRepository) {

    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        phone: String,
        email: String
    ): Result<Provisioned<Parent>> {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank()) {
            return Result.failure(IllegalArgumentException("Nombres, apellidos y correo son obligatorios"))
        }
        if (phone.isBlank()) {
            return Result.failure(IllegalArgumentException("El teléfono es obligatorio"))
        }
        return repository.registerParent(firstName.trim(), lastName.trim(), Phone(phone.trim()), Email(email.trim()))
    }
}

/** Registers a student and links it to an already registered parent. */
class RegisterChildUseCase @Inject constructor(private val repository: ParentRepository) {

    suspend operator fun invoke(
        parentId: ParentId?,
        firstName: String,
        lastName: String,
        age: String
    ): Result<Child> {
        if (parentId == null || parentId.value.isBlank()) {
            return Result.failure(IllegalArgumentException("Selecciona un padre ya registrado"))
        }
        if (firstName.isBlank() || lastName.isBlank()) {
            return Result.failure(IllegalArgumentException("Nombres y apellidos del estudiante son obligatorios"))
        }
        val parsedAge = age.trim().toIntOrNull()
        if (parsedAge == null || parsedAge !in 3..18) {
            return Result.failure(IllegalArgumentException("La edad debe estar entre 3 y 18 años"))
        }
        return repository.registerChild(parentId, firstName.trim(), lastName.trim(), parsedAge)
    }
}
