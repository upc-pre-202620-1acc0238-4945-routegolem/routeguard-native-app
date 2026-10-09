package pe.edu.upc.routeguard.stakeholderassetmanagement.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.DriverRepository
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.GroupRepository
import pe.edu.upc.routeguard.stakeholderassetmanagement.domain.ParentRepository
import pe.edu.upc.routeguard.stakeholderassetmanagement.infrastructure.remote.StakeholderApiService
import pe.edu.upc.routeguard.stakeholderassetmanagement.infrastructure.repositories.DriverRepositoryImpl
import pe.edu.upc.routeguard.stakeholderassetmanagement.infrastructure.repositories.GroupRepositoryImpl
import pe.edu.upc.routeguard.stakeholderassetmanagement.infrastructure.repositories.ParentRepositoryImpl
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
