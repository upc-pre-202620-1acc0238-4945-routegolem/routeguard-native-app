package pe.edu.upc.routeguard.features.stakeholder.domain

interface DriverRepository {
    suspend fun getDrivers(): Result<List<Driver>>

    /** Provisions the driver account (automatic credentials) and creates the driver profile. */
    suspend fun registerDriver(
        firstName: String,
        lastName: String,
        phone: String,
        licenseNumber: String,
        email: String
    ): Result<Provisioned<Driver>>
}

interface ParentRepository {
    suspend fun getParents(): Result<List<Parent>>

    /** Provisions the parent account (automatic credentials) and creates the parent profile. */
    suspend fun registerParent(
        firstName: String,
        lastName: String,
        phone: String,
        email: String
    ): Result<Provisioned<Parent>>

    suspend fun registerChild(parentId: String, firstName: String, lastName: String, age: Int): Result<Child>
}

interface GroupRepository {
    suspend fun getGroups(): Result<List<Group>>

    suspend fun createGroup(name: String): Result<Group>

    suspend fun includeStudent(groupId: String, studentId: String): Result<Group>

    suspend fun finalizeGroup(groupId: String): Result<Group>
}
