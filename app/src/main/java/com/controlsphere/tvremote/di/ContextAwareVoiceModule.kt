package com.controlsphere.tvremote.di

import com.controlsphere.tvremote.data.voice.ContextAwareVoiceService
import com.controlsphere.tvremote.data.voice.VoiceRepository
import com.controlsphere.tvremote.data.voice.AdvancedVoiceRepository
import com.controlsphere.tvremote.data.voice.CustomVoiceCommandRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContextAwareVoiceModule {
    
    @Provides
    @Singleton
    fun provideContextAwareVoiceService(
        voiceRepository: VoiceRepository,
        advancedVoiceRepository: AdvancedVoiceRepository,
        customVoiceCommandRepository: CustomVoiceCommandRepository
    ): ContextAwareVoiceService {
        return ContextAwareVoiceService(
            voiceRepository = voiceRepository,
            advancedVoiceRepository = advancedVoiceRepository,
            customVoiceCommandRepository = customVoiceCommandRepository
        )
    }
}
