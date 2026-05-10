package com.niked.fatless.di

import android.content.Context
import com.niked.fatless.core.bluetooth.BleManager
import com.niked.fatless.core.utils.AppLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class BluetoothModule {

    @Provides
    @Singleton
    fun provideBleManager(
        @ApplicationContext context: Context,
        logger: AppLogger
    ): BleManager {
        return BleManager(context, logger)
    }
}