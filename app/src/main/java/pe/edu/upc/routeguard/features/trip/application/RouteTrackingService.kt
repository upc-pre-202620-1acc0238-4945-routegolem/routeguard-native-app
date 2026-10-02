package pe.edu.upc.routeguard.features.trip.application

/** Port that keeps the GPS transmission alive in the background while a trip is active. */
interface RouteTrackingService {
    fun start()

    fun stop()
}
