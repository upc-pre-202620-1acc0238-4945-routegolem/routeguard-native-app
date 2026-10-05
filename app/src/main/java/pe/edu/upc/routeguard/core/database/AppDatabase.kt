package pe.edu.upc.routeguard.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import pe.edu.upc.routeguard.notifications.infrastructure.local.NotificationDao
import pe.edu.upc.routeguard.notifications.infrastructure.local.NotificationEntity
import pe.edu.upc.routeguard.trip.infrastructure.local.BoardingRecordEntity
import pe.edu.upc.routeguard.trip.infrastructure.local.LocationRecordEntity
import pe.edu.upc.routeguard.trip.infrastructure.local.TripDao
import pe.edu.upc.routeguard.trip.infrastructure.local.TripEntity
import pe.edu.upc.routeguard.trip.infrastructure.local.WaypointEntity

@Database(
    entities = [
        TripEntity::class,
        WaypointEntity::class,
        LocationRecordEntity::class,
        BoardingRecordEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun notificationDao(): NotificationDao
}
