package pe.edu.upc.routeguard.features.stakeholder.infrastructure.repositories

import pe.edu.upc.routeguard.features.stakeholder.domain.Child
import pe.edu.upc.routeguard.features.stakeholder.domain.Driver
import pe.edu.upc.routeguard.features.stakeholder.domain.Group
import pe.edu.upc.routeguard.features.stakeholder.domain.GroupStatus
import pe.edu.upc.routeguard.features.stakeholder.domain.Parent
import pe.edu.upc.routeguard.features.stakeholder.infrastructure.remote.ChildDto
import pe.edu.upc.routeguard.features.stakeholder.infrastructure.remote.DriverDto
import pe.edu.upc.routeguard.features.stakeholder.infrastructure.remote.ParentDto
import pe.edu.upc.routeguard.features.stakeholder.infrastructure.remote.StudentGroupDto

fun DriverDto.toDomain() = Driver(
    id = id,
    userId = userId,
    fullName = fullName ?: "$firstName $lastName",
    phone = phoneNumber.orEmpty(),
    licenseNumber = licenseNumber,
    email = email
)

fun ChildDto.toDomain(parentId: String) = Child(
    id = id,
    fullName = fullName ?: "$firstName $lastName",
    age = age,
    parentId = parentId
)

fun ParentDto.toDomain() = Parent(
    id = id,
    userId = userId,
    fullName = fullName ?: "$firstName $lastName",
    phone = phoneNumber.orEmpty(),
    email = email,
    children = children.orEmpty().map { it.toDomain(parentId = id) }
)

fun StudentGroupDto.toDomain() = Group(
    id = id,
    name = name,
    studentIds = childIds.orEmpty(),
    status = if (isFinalized) GroupStatus.FINALIZED else GroupStatus.DRAFT
)
