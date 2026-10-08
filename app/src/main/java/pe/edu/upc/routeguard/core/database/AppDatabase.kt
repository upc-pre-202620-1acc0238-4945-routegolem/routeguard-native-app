package pe.edu.upc.routeguard.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import pe.edu.upc.routeguard.notificationscommunication.infrastructure.local.NotificationDao
import pe.edu.upc.routeguard.notificationscommunication.infrastructure.local.NotificationEntity
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local.BoardingRecordEntity
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local.LocationRecordEntity
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local.TripDao
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local.TripEntity
import pe.edu.upc.routeguard.tripexecutionmonitoring.infrastructure.local.WaypointEntity

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
