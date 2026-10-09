package pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.routeguard.subscriptionplanmanagement.domain.SubscriptionRepository
import pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.remote.SubscriptionApiService
import pe.edu.upc.routeguard.subscriptionplanmanagement.infrastructure.repositories.SubscriptionRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SubscriptionApiModule {

    @Provides
    @Singleton
    fun provideSubscriptionApiService(retrofit: Retrofit): SubscriptionApiService =
        retrofit.create(SubscriptionApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
interface SubscriptionRepositoryModule {

    @Binds
    fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository
}
