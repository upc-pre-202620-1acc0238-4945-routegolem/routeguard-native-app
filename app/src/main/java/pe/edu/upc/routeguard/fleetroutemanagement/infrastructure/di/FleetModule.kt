package pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.routeguard.fleetroutemanagement.application.AddressSearch
import pe.edu.upc.routeguard.fleetroutemanagement.application.RoutePlanner
import pe.edu.upc.routeguard.fleetroutemanagement.domain.FleetRepository
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.map.MapboxFleetAdapter
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.remote.FleetApiService
import pe.edu.upc.routeguard.fleetroutemanagement.infrastructure.repositories.FleetRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FleetApiModule {

    @Provides
    @Singleton
    fun provideFleetApiService(retrofit: Retrofit): FleetApiService =
        retrofit.create(FleetApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
interface FleetRepositoryModule {

    @Binds
    fun bindFleetRepository(impl: FleetRepositoryImpl): FleetRepository

    @Binds
    fun bindAddressSearch(impl: MapboxFleetAdapter): AddressSearch

    @Binds
    fun bindRoutePlanner(impl: MapboxFleetAdapter): RoutePlanner
}
