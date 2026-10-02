package pe.edu.upc.routeguard.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import pe.edu.upc.routeguard.core.network.mapbox.MapboxApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapboxModule {

    /** Own client without the AuthInterceptor: the RouteGuard JWT must never reach Mapbox. */
    @Provides
    @Singleton
    @Named("mapbox")
    fun provideMapboxApi(): MapboxApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://api.mapbox.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MapboxApiService::class.java)
    }
}
