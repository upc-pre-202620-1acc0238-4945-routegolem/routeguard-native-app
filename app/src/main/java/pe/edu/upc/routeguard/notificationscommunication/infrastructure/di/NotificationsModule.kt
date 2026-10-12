package pe.edu.upc.routeguard.notificationscommunication.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.routeguard.core.database.AppDatabase
import pe.edu.upc.routeguard.notificationscommunication.domain.NotificationRepository
import pe.edu.upc.routeguard.notificationscommunication.domain.PushNotificationAdapter
import pe.edu.upc.routeguard.notificationscommunication.infrastructure.local.NotificationDao
import pe.edu.upc.routeguard.notificationscommunication.infrastructure.push.FcmPushAdapter
import pe.edu.upc.routeguard.notificationscommunication.infrastructure.remote.NotificationApiService
import pe.edu.upc.routeguard.notificationscommunication.infrastructure.repositories.NotificationRepositoryImpl
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationsProvidesModule {

    @Provides
    @Singleton
    fun provideNotificationApiService(retrofit: Retrofit): NotificationApiService =
        retrofit.create(NotificationApiService::class.java)

    @Provides
    @Singleton
    fun provideNotificationDao(database: AppDatabase): NotificationDao = database.notificationDao()
}

@Module
@InstallIn(SingletonComponent::class)
interface NotificationsBindsModule {

    @Binds
    fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    fun bindPushNotificationAdapter(impl: FcmPushAdapter): PushNotificationAdapter
}
