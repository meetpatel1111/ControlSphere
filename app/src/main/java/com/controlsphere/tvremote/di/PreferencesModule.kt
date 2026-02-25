package com.controlsphere.tvremote.di

import com.controlsphere.tvremote.data.preferences.PreferencesManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {
    
    @Binds
    @Singleton
    abstract fun bindPreferencesManager(
        preferencesManager: PreferencesManager
    ): PreferencesManager
}
