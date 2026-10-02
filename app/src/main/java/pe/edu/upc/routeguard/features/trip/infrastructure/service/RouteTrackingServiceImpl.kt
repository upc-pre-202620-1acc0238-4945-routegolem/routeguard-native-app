package pe.edu.upc.routeguard.features.trip.infrastructure.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import pe.edu.upc.routeguard.features.trip.application.RouteTrackingService
import javax.inject.Inject

/** Starts and stops the foreground service that streams the GPS in the background. */
class RouteTrackingServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RouteTrackingService {

    override fun start() {
        ContextCompat.startForegroundService(context, Intent(context, TrackingForegroundService::class.java))
    }

    override fun stop() {
        context.stopService(Intent(context, TrackingForegroundService::class.java))
    }
}
