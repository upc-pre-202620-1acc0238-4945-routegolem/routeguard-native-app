package pe.edu.upc.routeguard.core.network

import okhttp3.Interceptor
import okhttp3.Response
import pe.edu.upc.routeguard.core.session.SessionManager
import javax.inject.Inject

/** Adds the JWT of the active session to every request. */
class AuthInterceptor @Inject constructor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        sessionManager.token()?.let { builder.addHeader("Authorization", "Bearer $it") }
        return chain.proceed(builder.build())
    }
}
