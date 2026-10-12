package pe.edu.upc.routeguard.identityaccessmanagement.domain.valueobject

@JvmInline value class AccountId(val value: String)
@JvmInline value class Email(val value: String) { override fun toString() = value }
@JvmInline value class OrganizationId(val value: String)
@JvmInline value class ProfileId(val value: String)
