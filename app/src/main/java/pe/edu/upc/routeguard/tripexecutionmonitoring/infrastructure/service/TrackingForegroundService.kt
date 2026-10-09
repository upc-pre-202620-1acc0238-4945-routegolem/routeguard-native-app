package pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import pe.edu.upc.routeguard.tripexecutionmonitoring.application.GetActiveTripUseCase
import pe.edu.upc.routeguard.tripexecutionmonitoring.application.SendLocationUpdateUseCase
import pe.edu.upc.routeguard.tripexecutionmonitoring.application.SyncOfflineRecordsUseCase
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.Telemetry
import pe.edu.upc.routeguard.shared.domain.Coordinates
import javax.inject.Inject

/**
 * Background GPS transmission: the driver does not have to touch the phone while driving.
 * Every point is stored locally first, so nothing is lost when the signal drops.
 */
@AndroidEntryPoint
class TrackingForegroundService : Service() {

    @Inject lateinit var getActiveTrip: GetActiveTripUseCase
    @Inject lateinit var sendLocationUpdate: SendLocationUpdateUseCase
    @Inject lateinit var syncOfflineRecords: SyncOfflineRecordsUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var locationManager: LocationManager? = null
    private var updatesSinceSync = 0

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            scope.launch { handle(location) }
        }

        override fun onProviderEnabled(provider: String) = Unit

        override fun onProviderDisabled(provider: String) = Unit

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
        if (!requestUpdates()) stopSelf()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun requestUpdates(): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return false

        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager = manager
        manager.removeUpdates(listener)
        manager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            UPDATE_INTERVAL_MS,
            MIN_DISTANCE_METERS,
            listener,
            Looper.getMainLooper()
        )
        return true
    }

    private suspend fun handle(location: Location) {
        val trip = getActiveTrip()
        if (trip == null) {
            stopSelf()
            return
        }

        val battery = (getSystemService(Context.BATTERY_SERVICE) as BatteryManager)
            .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        sendLocationUpdate(
            trip = trip,
            coordinates = Coordinates(location.latitude, location.longitude),
            telemetry = Telemetry(
                speedKmh = location.speed * 3.6,
                batteryLevel = battery,
                heading = location.bearing.toDouble()
            )
        )

        updatesSinceSync++
        if (updatesSinceSync >= SYNC_EVERY) {
            updatesSinceSync = 0
            syncOfflineRecords(trip.id)
        }
    }

    private fun createChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Viaje en curso", NotificationManager.IMPORTANCE_LOW)
        )
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("RouteGuard")
            .setContentText("Compartiendo tu ubicación con los padres")
            .setOngoing(true)
            .build()

    override fun onDestroy() {
        locationManager?.removeUpdates(listener)
        scope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val CHANNEL_ID = "trip_tracking"
        const val NOTIFICATION_ID = 1001
        const val UPDATE_INTERVAL_MS = 5_000L
        const val MIN_DISTANCE_METERS = 10f
        const val SYNC_EVERY = 10
    }
}
