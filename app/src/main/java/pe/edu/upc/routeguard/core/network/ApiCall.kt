package pe.edu.upc.routeguard.core.network

import com.google.gson.JsonParser
import retrofit2.Response

class ApiException(val code: Int, message: String) : Exception(message)

/** Runs a Retrofit call and maps it to a Result with a readable error message. */
suspend fun <T> safeApiCall(block: suspend () -> Response<T>): Result<T> {
    return try {
        val response = block()
        val body = response.body()
        when {
            response.isSuccessful && body != null -> Result.success(body)
            response.isSuccessful -> Result.failure(ApiException(response.code(), "Respuesta vacía del servidor"))
            else -> Result.failure(ApiException(response.code(), errorMessage(response)))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/** Reads the RFC 7807 problem details returned by the backend (`detail` or `title`). */
private fun errorMessage(response: Response<*>): String {
    val raw = try {
        response.errorBody()?.string()
    } catch (_: Exception) {
        null
    }
    val parsed = raw?.let {
        try {
            val json = JsonParser.parseString(it).asJsonObject
            (json.get("detail") ?: json.get("title") ?: json.get("message"))?.takeIf { e -> !e.isJsonNull }?.asString
        } catch (_: Exception) {
            null
        }
    }
    return parsed?.takeIf { it.isNotBlank() }
        ?: response.message().takeIf { it.isNotBlank() }
        ?: "Error ${response.code()}"
}
