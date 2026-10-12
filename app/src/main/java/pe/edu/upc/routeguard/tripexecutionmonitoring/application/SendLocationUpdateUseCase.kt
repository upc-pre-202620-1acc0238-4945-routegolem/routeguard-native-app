package pe.edu.upc.routeguard.tripexecutionmonitoring.application

import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.LocationRecord
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Telemetry
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Trip
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.TripRepository
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.LocationRecordId
import pe.edu.upc.routeguard.shared.domain.Coordinates
import java.util.UUID
import javax.inject.Inject

class SendLocationUpdateUseCase @Inject constructor(private val repository: TripRepository) {

    /** A completed or archived trip does not accept new records. */
    suspend operator fun invoke(trip: Trip, coordinates: Coordinates, telemetry: Telemetry): Boolean {
        if (!trip.acceptsRecords) return false
        repository.recordLocation(
            LocationRecord(
                id = LocationRecordId(UUID.randomUUID().toString()),
                tripId = trip.id,
                coordinates = coordinates,
                telemetry = telemetry,
                recordedAt = System.currentTimeMillis()
            )
        )
        return true
    }
}
