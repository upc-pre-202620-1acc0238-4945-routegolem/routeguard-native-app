package pe.edu.upc.routeguard.features.trip.application

import pe.edu.upc.routeguard.features.trip.domain.OfflineSyncCompleted
import pe.edu.upc.routeguard.features.trip.domain.TripRepository
import javax.inject.Inject

/** Delivers the location and boarding records saved while the device had no signal. */
class SyncOfflineRecordsUseCase @Inject constructor(private val repository: TripRepository) {

    suspend operator fun invoke(tripId: String): Result<OfflineSyncCompleted> =
        repository.syncOfflineRecords(tripId)
}
