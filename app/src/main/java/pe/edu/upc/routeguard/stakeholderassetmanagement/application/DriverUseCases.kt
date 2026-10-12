package pe.edu.upc.routeguard.stakeholderassetmanagement.application

import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.Driver
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.DriverRepository
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.Provisioned
import javax.inject.Inject

class GetDriversUseCase @Inject constructor(private val repository: DriverRepository) {

    suspend operator fun invoke() = repository.getDrivers()
}

class RegisterDriverUseCase @Inject constructor(private val repository: DriverRepository) {

    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        phone: String,
        licenseNumber: String,
        email: String
    ): Result<Provisioned<Driver>> {
        if (firstName.isBlank() || lastName.isBlank() || licenseNumber.isBlank() || email.isBlank()) {
            return Result.failure(IllegalArgumentException("Nombres, apellidos, licencia y correo son obligatorios"))
        }
        if (phone.isBlank()) {
            return Result.failure(IllegalArgumentException("El teléfono es obligatorio"))
        }
        return repository.registerDriver(
            firstName.trim(), lastName.trim(), pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.Phone(phone.trim()), pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.LicenseNumber(licenseNumber.trim()), pe.edu.upc.routeguard.stakeholderassetmanagement.domain.valueobject.Email(email.trim())
        )
    }
}
