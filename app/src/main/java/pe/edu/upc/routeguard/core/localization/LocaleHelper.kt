package pe.edu.upc.routeguard.core.localization

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import pe.edu.upc.routeguard.core.session.SessionManager
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

object LocaleHelper {
    const val LANG_ES = "es"
    const val LANG_EN = "en"

    fun normalize(code: String): String =
        if (code.equals(LANG_EN, ignoreCase = true)) LANG_EN else LANG_ES

    fun localeFor(code: String): Locale = when (normalize(code)) {
        LANG_EN -> Locale.ENGLISH
        else -> Locale.forLanguageTag("es")
    }

    fun localizedContext(base: Context, languageCode: String): Context {
        val locale = localeFor(languageCode)
        val config = Configuration(base.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        return base.createConfigurationContext(config)
    }
}

/**
 * Domain/Application validation or state exception carrying a localized string resource ID
 * plus a fallback message for pure JVM unit tests.
 */
class LocalizedException(
    @StringRes val resId: Int,
    val formatArgs: List<Any> = emptyList(),
    fallback: String
) : IllegalStateException(fallback)

class LocalizedActivationException(
    val missingResIds: List<Int>,
    fallback: String
) : IllegalStateException(fallback)

@Singleton
class AppStringsProvider @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val sessionManager: SessionManager
) {
    fun currentContext(): Context =
        LocaleHelper.localizedContext(appContext, sessionManager.language())

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String =
        if (formatArgs.isEmpty()) currentContext().getString(resId)
        else currentContext().getString(resId, *formatArgs)

    fun resolveError(error: Throwable, @StringRes fallbackRes: Int): String = when (error) {
        is LocalizedException -> getString(error.resId, *error.formatArgs.toTypedArray())
        is LocalizedActivationException -> {
            val missing = error.missingResIds.joinToString(", ") { getString(it) }
            getString(pe.edu.upc.routeguard.R.string.fleet_error_missing_to_activate, missing)
        }
        else -> error.message?.takeIf { it.isNotBlank() } ?: getString(fallbackRes)
    }
}
