package com.controlsphere.tvremote.di

import com.controlsphere.tvremote.data.discovery.DeviceDiscovery
import com.controlsphere.tvremote.data.discovery.DeviceDiscoveryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DiscoveryModule {
    
    @Binds
    @Singleton
    abstract fun bindDeviceDiscovery(
        deviceDiscoveryImpl: DeviceDiscoveryImpl
    ): DeviceDiscovery
}
