package pe.edu.upc.routeguard.notificationscommunication.presentation.alerts

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.routeguard.notificationscommunication.domain.Notification
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationStatus
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationType
import pe.edu.upc.routeguard.notificationscommunication.domain.valueobject.NotificationId
import pe.edu.upc.routeguard.tripexecutionmonitoring.domain.valueobject.TripId

class AlertFilterTest {

    private fun sample(id: String, type: NotificationType, hasPanic: Boolean = false) = Notification(
        id = NotificationId(id),
        tripId = TripId("trip-1"),
        type = type,
        status = NotificationStatus.DELIVERED,
        title = type.name,
        body = "Body $id",
        createdAt = 1_700_000_000_000L,
        hasPanicAlert = hasPanic
    )

    @Test
    fun filtersCorrectlyAcrossAllCategories() {
        val panic = sample("1", NotificationType.PANIC_ALERT, hasPanic = true)
        val incident = sample("2", NotificationType.INCIDENT_REPORTED)
        val boarded = sample("3", NotificationType.STUDENT_BOARDED)
        val geofence = sample("4", NotificationType.GEOFENCE_BREACHED)
        val tripStarted = sample("5", NotificationType.TRIP_STARTED)
        val announcement = sample("6", NotificationType.ANNOUNCEMENT)
        val general = sample("7", NotificationType.GENERAL)

        val all = listOf(panic, incident, boarded, geofence, tripStarted, announcement, general)

        val uiState = NotificationsUiState(notifications = all)
        assertEquals(7, uiState.countFor(AlertFilter.ALL))
        assertEquals(2, uiState.countFor(AlertFilter.PRIORITY))
        assertEquals(3, uiState.countFor(AlertFilter.TRIP))
        assertEquals(2, uiState.countFor(AlertFilter.NOTICES))
        assertEquals(0, uiState.unreadCount)

        val withUnread = uiState.copy(
            notifications = all + sample("8", NotificationType.ANNOUNCEMENT).copy(status = NotificationStatus.DISPATCHED)
        )
        assertEquals(1, withUnread.unreadCount)

        assertEquals(7, NotificationsUiState(notifications = all, selectedFilter = AlertFilter.ALL).filteredNotifications.size)
        assertEquals(listOf(panic, incident), NotificationsUiState(notifications = all, selectedFilter = AlertFilter.PRIORITY).filteredNotifications)
        assertEquals(listOf(boarded, geofence, tripStarted), NotificationsUiState(notifications = all, selectedFilter = AlertFilter.TRIP).filteredNotifications)
        assertEquals(listOf(announcement, general), NotificationsUiState(notifications = all, selectedFilter = AlertFilter.NOTICES).filteredNotifications)
    }
}
