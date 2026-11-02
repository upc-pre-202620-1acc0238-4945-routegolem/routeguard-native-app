package pe.edu.upc.routeguard.core.designsystem.components.assistant

import androidx.annotation.StringRes
import pe.edu.upc.routeguard.R
import pe.edu.upc.routeguard.identityaccessmanagement.domain.Role

/**
 * Represents an interactive onboarding step explained by the RouteGuard assistant mascot.
 *
 * @param titleRes String resource for the step heading.
 * @param messageRes String resource for the step explanation dialog.
 * @param targetTabIndex Optional bottom navigation tab index to switch active screen and highlight context.
 */
data class AssistantStep(
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
    val targetTabIndex: Int? = null
)

/**
 * Provides role-tailored tutorial itineraries according to DDD bounded context visibility.
 */
object AssistantTutorialProvider {

    fun stepsFor(role: Role): List<AssistantStep> = when (role) {
        Role.ADMINISTRATOR -> listOf(
            AssistantStep(
                titleRes = R.string.assistant_title_admin_welcome,
                messageRes = R.string.assistant_msg_admin_welcome,
                targetTabIndex = null
            ),
            AssistantStep(
                titleRes = R.string.assistant_title_admin_people,
                messageRes = R.string.assistant_msg_admin_people,
                targetTabIndex = 0
            ),
            AssistantStep(
                titleRes = R.string.assistant_title_admin_routes,
                messageRes = R.string.assistant_msg_admin_routes,
                targetTabIndex = 1
            ),
            AssistantStep(
                titleRes = R.string.assistant_title_admin_live,
                messageRes = R.string.assistant_msg_admin_live,
                targetTabIndex = 2
            ),
            AssistantStep(
                titleRes = R.string.assistant_title_admin_plans,
                messageRes = R.string.assistant_msg_admin_plans,
                targetTabIndex = 3
            ),
            AssistantStep(
                titleRes = R.string.assistant_title_admin_profile,
                messageRes = R.string.assistant_msg_admin_profile,
                targetTabIndex = 4
            )
        )

        Role.DRIVER -> listOf(
            AssistantStep(
                titleRes = R.string.assistant_title_driver_welcome,
                messageRes = R.string.assistant_msg_driver_welcome,
                targetTabIndex = null
            ),
            AssistantStep(
                titleRes = R.string.assistant_title_driver_trip,
                messageRes = R.string.assistant_msg_driver_trip,
                targetTabIndex = 0
            ),
            AssistantStep(
                titleRes = R.string.assistant_title_driver_alerts,
                messageRes = R.string.assistant_msg_driver_alerts,
                targetTabIndex = 1
            ),
            AssistantStep(
                titleRes = R.string.assistant_title_driver_profile,
                messageRes = R.string.assistant_msg_driver_profile,
                targetTabIndex = 2
            )
        )

        Role.PARENT -> listOf(
            AssistantStep(
                titleRes = R.string.assistant_title_parent_welcome,
                messageRes = R.string.assistant_msg_parent_welcome,
                targetTabIndex = null
            ),
            AssistantStep(
                titleRes = R.string.assistant_title_parent_tracking,
                messageRes = R.string.assistant_msg_parent_tracking,
                targetTabIndex = 0
            ),
            AssistantStep(
                titleRes = R.string.assistant_title_parent_alerts,
                messageRes = R.string.assistant_msg_parent_alerts,
                targetTabIndex = 1
            ),
            AssistantStep(
                titleRes = R.string.assistant_title_parent_profile,
                messageRes = R.string.assistant_msg_parent_profile,
                targetTabIndex = 2
            )
        )
    }
}
