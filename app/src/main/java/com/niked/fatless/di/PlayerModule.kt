package com.niked.fatless.di

import com.niked.fatless.data.player.AndroidAudioPlayer
import com.niked.fatless.domain.player.IAudioPlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindAudioPlayer(
        impl: AndroidAudioPlayer
    ): IAudioPlayer
}