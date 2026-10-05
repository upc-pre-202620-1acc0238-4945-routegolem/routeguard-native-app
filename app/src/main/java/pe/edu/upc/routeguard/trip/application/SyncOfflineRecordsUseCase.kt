package pe.edu.upc.routeguard.trip.application

import pe.edu.upc.routeguard.trip.domain.OfflineSyncCompleted
import pe.edu.upc.routeguard.trip.domain.TripRepository
import pe.edu.upc.routeguard.trip.domain.valueobject.TripId
import javax.inject.Inject

/** Delivers the location and boarding records saved while the device had no signal. */
class SyncOfflineRecordsUseCase @Inject constructor(private val repository: TripRepository) {

    suspend operator fun invoke(tripId: TripId): Result<OfflineSyncCompleted> =
        repository.syncOfflineRecords(tripId)
}
