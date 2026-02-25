package com.controlsphere.tvremote.di

import com.controlsphere.tvremote.data.voice.GeminiVoiceService
import com.controlsphere.tvremote.data.voice.VoiceService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceModule {
    
    @Binds
    @Singleton
    abstract fun bindVoiceService(
        geminiVoiceService: GeminiVoiceService
    ): VoiceService
}
