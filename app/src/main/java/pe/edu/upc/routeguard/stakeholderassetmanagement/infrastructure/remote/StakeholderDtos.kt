package pe.edu.upc.routeguard.stakeholderassetmanagement.infrastructure.remote

data class DriverDto(
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["driverId", "driver_id"])
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
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["childId", "child_id"])
    val id: String,
    val firstName: String,
    val lastName: String,
    val fullName: String?,
    val age: Int
)

data class ParentDto(
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["parentId", "parent_id"])
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
    @com.google.gson.annotations.SerializedName(value = "id", alternate = ["studentGroupId", "studentGroup_id", "student_group_id"])
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
