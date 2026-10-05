package pe.edu.upc.routeguard.stakeholder.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.routeguard.stakeholder.domain.DriverRepository
import pe.edu.upc.routeguard.stakeholder.domain.GroupRepository
import pe.edu.upc.routeguard.stakeholder.domain.ParentRepository
import pe.edu.upc.routeguard.stakeholder.infrastructure.remote.StakeholderApiService
import pe.edu.upc.routeguard.stakeholder.infrastructure.repositories.DriverRepositoryImpl
import pe.edu.upc.routeguard.stakeholder.infrastructure.repositories.GroupRepositoryImpl
import pe.edu.upc.routeguard.stakeholder.infrastructure.repositories.ParentRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StakeholderApiModule {

    @Provides
    @Singleton
    fun provideStakeholderApiService(retrofit: Retrofit): StakeholderApiService =
        retrofit.create(StakeholderApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
interface StakeholderRepositoryModule {

    @Binds
    fun bindDriverRepository(impl: DriverRepositoryImpl): DriverRepository

    @Binds
    fun bindParentRepository(impl: ParentRepositoryImpl): ParentRepository

    @Binds
    fun bindGroupRepository(impl: GroupRepositoryImpl): GroupRepository
}
