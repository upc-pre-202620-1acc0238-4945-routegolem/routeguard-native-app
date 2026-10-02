package pe.edu.upc.routeguard.features.trip.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.routeguard.core.database.AppDatabase
import pe.edu.upc.routeguard.features.trip.application.MapboxAdapter
import pe.edu.upc.routeguard.features.trip.application.RouteTrackingService
import pe.edu.upc.routeguard.features.trip.domain.TripRepository
import pe.edu.upc.routeguard.features.trip.infrastructure.local.TripDao
import pe.edu.upc.routeguard.features.trip.infrastructure.map.MapboxAdapterImpl
import pe.edu.upc.routeguard.features.trip.infrastructure.remote.TripApiService
import pe.edu.upc.routeguard.features.trip.infrastructure.repositories.TripRepositoryImpl
import pe.edu.upc.routeguard.features.trip.infrastructure.service.RouteTrackingServiceImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TripProvidesModule {

    @Provides
    @Singleton
    fun provideTripApiService(retrofit: Retrofit): TripApiService =
        retrofit.create(TripApiService::class.java)

    @Provides
    @Singleton
    fun provideTripDao(database: AppDatabase): TripDao = database.tripDao()
}

@Module
@InstallIn(SingletonComponent::class)
interface TripBindsModule {

    @Binds
    fun bindTripRepository(impl: TripRepositoryImpl): TripRepository

    @Binds
    fun bindMapboxAdapter(impl: MapboxAdapterImpl): MapboxAdapter

    @Binds
    fun bindRouteTrackingService(impl: RouteTrackingServiceImpl): RouteTrackingService
}
