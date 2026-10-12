package pe.edu.upc.routeguard.core.network

import okhttp3.Interceptor
import okhttp3.Response
import pe.edu.upc.routeguard.core.session.SessionManager
import javax.inject.Inject

/** Adds the JWT of the active session to every request. */
class AuthInterceptor @Inject constructor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.token()
        val builder = chain.request().newBuilder()
        token?.let { builder.addHeader("Authorization", "Bearer $it") }
        val response = chain.proceed(builder.build())
        // The API requires a valid JWT: a 401 to a request that carried it means the session is over.
        if (response.code == 401 && token != null) sessionManager.onTokenRejected()
        return response
    }
}
