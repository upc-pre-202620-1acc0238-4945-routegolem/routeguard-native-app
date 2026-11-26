package pe.edu.upc.routeguard.core.session

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upc.routeguard.core.localization.LocaleHelper
import javax.inject.Inject
import javax.inject.Singleton

/** Keeps the active session (JWT + role + tenant) in private shared preferences. */
@Singleton
class SessionManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("routeguard_session", Context.MODE_PRIVATE)

    private val _sessionRejected = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emits when the server rejects the JWT of the active session (expired or no longer valid). */
    val sessionRejected: SharedFlow<Unit> = _sessionRejected.asSharedFlow()

    private val _languageFlow = MutableStateFlow(
        LocaleHelper.normalize(prefs.getString(KEY_LANGUAGE, LocaleHelper.LANG_ES) ?: LocaleHelper.LANG_ES)
    )

    /** Active UI language code ("es" or "en"), persisted across sign-out. */
    val languageFlow: StateFlow<String> = _languageFlow.asStateFlow()

    fun language(): String = _languageFlow.value

    fun setLanguage(code: String) {
        val normalized = LocaleHelper.normalize(code)
        prefs.edit().putString(KEY_LANGUAGE, normalized).apply()
        _languageFlow.value = normalized
    }

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

    /** Signing out keeps [cacheOwner] and the user's chosen language across sessions. */
    fun clear() {
        val owner = cacheOwner()
        val lang = language()
        prefs.edit().clear().apply()
        prefs.edit().apply {
            if (owner != null) putString(KEY_CACHE_OWNER, owner)
            putString(KEY_LANGUAGE, lang)
        }.apply()
    }

    /** Id of the user whose data is cached on this device (notifications, ...). */
    fun cacheOwner(): String? = prefs.getString(KEY_CACHE_OWNER, null)

    fun setCacheOwner(userId: String) {
        prefs.edit().putString(KEY_CACHE_OWNER, userId).apply()
    }

    /** Whether the user has completed or dismissed the interactive mascot tutorial. */
    fun hasSeenTutorial(userId: String): Boolean = prefs.getBoolean(KEY_TUTORIAL_SEEN_PREFIX + userId, false)

    fun setTutorialSeen(userId: String, seen: Boolean = true) {
        prefs.edit().putBoolean(KEY_TUTORIAL_SEEN_PREFIX + userId, seen).apply()
    }

    /** The server answered 401 to a request that carried the token: drop the session and notify the UI. */
    fun onTokenRejected() {
        if (token() == null) return
        clear()
        _sessionRejected.tryEmit(Unit)
    }

    private companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_NAME = "full_name"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
        const val KEY_TOKEN = "token"
        const val KEY_ORGANIZATION_ID = "organization_id"
        const val KEY_PROFILE_ID = "profile_id"
        const val KEY_CACHE_OWNER = "cache_owner"
        const val KEY_TUTORIAL_SEEN_PREFIX = "tutorial_seen_"
        const val KEY_LANGUAGE = "app_language"
    }
}
