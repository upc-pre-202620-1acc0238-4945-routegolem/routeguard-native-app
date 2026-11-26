package pe.edu.upc.routeguard.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import pe.edu.upc.routeguard.BuildConfig
import pe.edu.upc.routeguard.core.network.AuthInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val DEFAULT_BASE_URL = "https://routeguard-web-services.onrender.com/api/v1/"

    // Guard against blank or relative BuildConfig values when CI env secrets are unset
    internal fun resolveBaseUrl(configured: String): String {
        val candidate = configured.trim()
        val valid = candidate.takeIf {
            it.startsWith("http://") || it.startsWith("https://")
        } ?: DEFAULT_BASE_URL
        return valid.trimEnd('/') + "/"
    }

    private val BASE_URL: String = resolveBaseUrl(BuildConfig.API_BASE_URL)

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val gson = com.google.gson.GsonBuilder().create()

        return Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(gson))
            .build()
    }
}
