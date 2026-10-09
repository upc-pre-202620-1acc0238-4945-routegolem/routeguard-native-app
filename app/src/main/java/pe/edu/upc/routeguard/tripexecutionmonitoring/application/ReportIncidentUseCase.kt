package pe.edu.upc.routeguard.tripexecutionmonitoring.application

import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Incident
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.IncidentType
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Trip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripRepository
import javax.inject.Inject

class ReportIncidentUseCase @Inject constructor(private val repository: TripRepository) {

    suspend operator fun invoke(trip: Trip, type: IncidentType, description: String): Result<Unit> {
        if (!trip.isActive) {
            return Result.failure(IllegalStateException("No hay un viaje en curso"))
        }
        val incident = Incident(type = type, description = description.trim())
        if (incident.text.length !in 10..500) {
            return Result.failure(IllegalArgumentException("Describe la incidencia (mínimo 10 caracteres)"))
        }
        return repository.reportIncident(trip.id, incident)
    }
}
