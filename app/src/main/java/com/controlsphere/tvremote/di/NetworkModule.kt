package com.controlsphere.tvremote.di

import com.controlsphere.tvremote.data.adb.AdbConnection
import com.controlsphere.tvremote.data.adb.AdbConnectionImpl
import com.controlsphere.tvremote.data.repository.DeviceRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    
    @Binds
    @Singleton
    abstract fun bindAdbConnection(
        adbConnectionImpl: AdbConnectionImpl
    ): AdbConnection
    
    companion object {
        @Provides
        @Singleton
        fun provideGson(): Gson {
            return GsonBuilder()
                .setLenient()
                .create()
        }
    }
}
