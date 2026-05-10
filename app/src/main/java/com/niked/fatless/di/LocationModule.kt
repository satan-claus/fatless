package com.niked.fatless.di

import android.content.Context
import android.location.LocationManager
import com.niked.fatless.data.location.DefaultLocationClient
import com.niked.fatless.domain.location.ILocationClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
        return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @Provides
    @Singleton
    fun provideLocationClient(
        @ApplicationContext context: Context,
        locationManager: LocationManager
    ): ILocationClient {
        return DefaultLocationClient(context, locationManager)
    }
}
