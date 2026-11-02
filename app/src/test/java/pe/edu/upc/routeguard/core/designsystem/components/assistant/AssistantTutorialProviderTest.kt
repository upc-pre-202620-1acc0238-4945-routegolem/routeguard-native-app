package pe.edu.upc.routeguard.core.designsystem.components.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.routeguard.identityaccessmanagement.domain.Role

class AssistantTutorialProviderTest {

    @Test
    fun `administrator tutorial steps are role-appropriate and map to admin tabs`() {
        val steps = AssistantTutorialProvider.stepsFor(Role.ADMINISTRATOR)
        assertEquals(6, steps.size)

        // First step is the welcome overview
        assertNull(steps[0].targetTabIndex)
        assertTrue(steps[0].titleRes != 0)
        assertTrue(steps[0].messageRes != 0)

        // Following steps map to people, routes, live monitoring, subscriptions, profile
        assertEquals(0, steps[1].targetTabIndex)
        assertEquals(1, steps[2].targetTabIndex)
        assertEquals(2, steps[3].targetTabIndex)
        assertEquals(3, steps[4].targetTabIndex)
        assertEquals(4, steps[5].targetTabIndex)
    }

    @Test
    fun `driver tutorial steps are role-appropriate and map to driver tabs`() {
        val steps = AssistantTutorialProvider.stepsFor(Role.DRIVER)
        assertEquals(4, steps.size)

        assertNull(steps[0].targetTabIndex)
        assertEquals(0, steps[1].targetTabIndex) // Trip
        assertEquals(1, steps[2].targetTabIndex) // Alerts
        assertEquals(2, steps[3].targetTabIndex) // Profile
    }

    @Test
    fun `parent tutorial steps are role-appropriate and map to parent tabs`() {
        val steps = AssistantTutorialProvider.stepsFor(Role.PARENT)
        assertEquals(4, steps.size)

        assertNull(steps[0].targetTabIndex)
        assertEquals(0, steps[1].targetTabIndex) // Tracking
        assertEquals(1, steps[2].targetTabIndex) // Alerts
        assertEquals(2, steps[3].targetTabIndex) // Profile
    }
}
