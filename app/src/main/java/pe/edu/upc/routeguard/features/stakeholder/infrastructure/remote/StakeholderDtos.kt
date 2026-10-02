package pe.edu.upc.routeguard.features.stakeholder.infrastructure.remote

data class DriverDto(
    val id: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val fullName: String?,
    val email: String,
    val phoneNumber: String?,
    val licenseNumber: String
)

data class ChildDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val fullName: String?,
    val age: Int
)

data class ParentDto(
    val id: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val fullName: String?,
    val email: String,
    val phoneNumber: String?,
    val children: List<ChildDto>?
)

data class StudentGroupDto(
    val id: String,
    val name: String,
    val childIds: List<String>?,
    val isFinalized: Boolean
)

data class CreateDriverRequestDto(
    val organizationId: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val licenseNumber: String
)

data class CreateParentRequestDto(
    val organizationId: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String
)

data class AddChildRequestDto(
    val firstName: String,
    val lastName: String,
    val age: Int
)

data class CreateStudentGroupRequestDto(
    val organizationId: String,
    val name: String
)

data class GroupChildRequestDto(val childId: String)
