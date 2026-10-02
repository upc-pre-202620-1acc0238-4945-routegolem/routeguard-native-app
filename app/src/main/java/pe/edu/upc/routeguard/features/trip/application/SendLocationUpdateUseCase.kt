package pe.edu.upc.routeguard.features.trip.application

import pe.edu.upc.routeguard.features.trip.domain.LocationRecord
import pe.edu.upc.routeguard.features.trip.domain.Telemetry
import pe.edu.upc.routeguard.features.trip.domain.Trip
import pe.edu.upc.routeguard.features.trip.domain.TripRepository
import pe.edu.upc.routeguard.shared.domain.Coordinates
import java.util.UUID
import javax.inject.Inject

class SendLocationUpdateUseCase @Inject constructor(private val repository: TripRepository) {

    /** A completed or archived trip does not accept new records. */
    suspend operator fun invoke(trip: Trip, coordinates: Coordinates, telemetry: Telemetry): Boolean {
        if (!trip.acceptsRecords) return false
        repository.recordLocation(
            LocationRecord(
                id = UUID.randomUUID().toString(),
                tripId = trip.id,
                coordinates = coordinates,
                telemetry = telemetry,
                recordedAt = System.currentTimeMillis()
            )
        )
        return true
    }
}
