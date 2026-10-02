package pe.edu.upc.routeguard.core.session

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Keeps the active session (JWT + role + tenant) in private shared preferences. */
@Singleton
class SessionManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("routeguard_session", Context.MODE_PRIVATE)

    fun save(session: UserSession) {
        prefs.edit()
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_NAME, session.fullName)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_ROLE, session.role)
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_ORGANIZATION_ID, session.organizationId)
            .putString(KEY_PROFILE_ID, session.profileId)
            .apply()
    }

    fun current(): UserSession? {
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        val role = prefs.getString(KEY_ROLE, null) ?: return null
        return UserSession(
            userId = prefs.getString(KEY_USER_ID, "") ?: "",
            fullName = prefs.getString(KEY_NAME, "") ?: "",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            role = role,
            token = token,
            organizationId = prefs.getString(KEY_ORGANIZATION_ID, "") ?: "",
            profileId = prefs.getString(KEY_PROFILE_ID, null)
        )
    }

    fun token(): String? = prefs.getString(KEY_TOKEN, null)

    /** Tenant of the signed-in user; every create request is scoped to it. */
    fun organizationId(): String = prefs.getString(KEY_ORGANIZATION_ID, "") ?: ""

    fun profileId(): String? = prefs.getString(KEY_PROFILE_ID, null)

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_NAME = "full_name"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
        const val KEY_TOKEN = "token"
        const val KEY_ORGANIZATION_ID = "organization_id"
        const val KEY_PROFILE_ID = "profile_id"
    }
}
