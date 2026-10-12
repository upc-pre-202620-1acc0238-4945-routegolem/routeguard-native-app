package pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject

@JvmInline value class DriverId(val value: String)
@JvmInline value class UserId(val value: String)
@JvmInline value class Phone(val value: String) { override fun toString() = value }
@JvmInline value class LicenseNumber(val value: String) { override fun toString() = value }
@JvmInline value class Email(val value: String) { override fun toString() = value }
@JvmInline value class ChildId(val value: String)
@JvmInline value class ParentId(val value: String)
@JvmInline value class GroupId(val value: String)
@JvmInline value class StudentId(val value: String)
