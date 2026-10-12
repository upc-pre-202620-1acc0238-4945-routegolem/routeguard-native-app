package pe.edu.upc.routeguard.stakeholderassetmanagement.infrastructure.repositories

import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.Child
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.Driver
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.Group
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.GroupStatus
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.Parent
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.ChildId
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.DriverId
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.Email
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.GroupId
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.LicenseNumber
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.ParentId
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.Phone
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.StudentId
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.UserId
import pe.edu.upc.routeguard.stakeholderassetmanagement.infrastructure.remote.ChildDto
import pe.edu.upc.routeguard.stakeholderassetmanagement.infrastructure.remote.DriverDto
import pe.edu.upc.routeguard.stakeholderassetmanagement.infrastructure.remote.ParentDto
import pe.edu.upc.routeguard.stakeholderassetmanagement.infrastructure.remote.StudentGroupDto

fun DriverDto.toDomain() = Driver(
    id = DriverId(id),
    userId = UserId(userId),
    fullName = fullName ?: "$firstName $lastName",
    phone = Phone(phoneNumber.orEmpty()),
    licenseNumber = LicenseNumber(licenseNumber),
    email = Email(email)
)

fun ChildDto.toDomain(parentId: String) = Child(
    id = ChildId(id),
    fullName = fullName ?: "$firstName $lastName",
    age = age,
    parentId = ParentId(parentId)
)

fun ParentDto.toDomain() = Parent(
    id = ParentId(id),
    userId = UserId(userId),
    fullName = fullName ?: "$firstName $lastName",
    phone = Phone(phoneNumber.orEmpty()),
    email = Email(email),
    children = children.orEmpty().map { it.toDomain(parentId = id) }
)

fun StudentGroupDto.toDomain() = Group(
    id = GroupId(id),
    name = name,
    studentIds = childIds.orEmpty().map { StudentId(it) },
    status = if (isFinalized) GroupStatus.FINALIZED else GroupStatus.DRAFT
)
