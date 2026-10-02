package pe.edu.upc.routeguard.features.iam.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.routeguard.features.iam.domain.AuthRepository
import pe.edu.upc.routeguard.features.iam.infrastructure.remote.IamApiService
import pe.edu.upc.routeguard.features.iam.infrastructure.repositories.AuthRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IamApiModule {

    @Provides
    @Singleton
    fun provideIamApiService(retrofit: Retrofit): IamApiService =
        retrofit.create(IamApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
interface IamRepositoryModule {

    @Binds
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
