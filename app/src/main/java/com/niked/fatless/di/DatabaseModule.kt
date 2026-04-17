package com.niked.fatless.di

import android.content.Context
import androidx.room.Room
import com.niked.fatless.data.local.AppDatabase
import com.niked.fatless.utils.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

//    @Provides
//    fun provideWorkoutDao(db: AppDatabase): WorkoutDao {
//        return db.workoutDao()
//    }
}