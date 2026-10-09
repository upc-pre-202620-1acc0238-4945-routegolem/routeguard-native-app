package pe.edu.upc.routeguard.stakeholderassetmanagement.domain

import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.Email
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.GroupId
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.LicenseNumber
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.ParentId
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.Phone
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.StudentId

interface DriverRepository {
    suspend fun getDrivers(): Result<List<Driver>>

    /** Provisions the driver account (automatic credentials) and creates the driver profile. */
    suspend fun registerDriver(
        firstName: String,
        lastName: String,
        phone: Phone,
        licenseNumber: LicenseNumber,
        email: Email
    ): Result<Provisioned<Driver>>
}

interface ParentRepository {
    suspend fun getParents(): Result<List<Parent>>

    /** Provisions the parent account (automatic credentials) and creates the parent profile. */
    suspend fun registerParent(
        firstName: String,
        lastName: String,
        phone: Phone,
        email: Email
    ): Result<Provisioned<Parent>>

    suspend fun registerChild(parentId: ParentId, firstName: String, lastName: String, age: Int): Result<Child>
}

interface GroupRepository {
    suspend fun getGroups(): Result<List<Group>>

    suspend fun createGroup(name: String): Result<Group>

    suspend fun includeStudent(groupId: GroupId, studentId: StudentId): Result<Group>

    suspend fun finalizeGroup(groupId: GroupId): Result<Group>
}
