package com.jobassistant.di

import android.content.Context
import androidx.room.Room
import com.jobassistant.data.db.AppDatabase
import com.jobassistant.data.db.Converters
import com.jobassistant.data.db.dao.CareerInsightsDao
import com.jobassistant.data.db.dao.JobApplicationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Replaces [DatabaseModule] in instrumented tests with an in-memory, unencrypted Room database.
 *
 * SQLCipher requires the Android Keystore which is unavailable or behaves unreliably on some
 * emulator configurations. Using an in-memory plain database in tests lets all HiltAndroidTest
 * UI tests run without SQLCipher native library initialisation.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideInMemoryDatabase(
        @ApplicationContext context: Context,
        converters: Converters
    ): AppDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .addTypeConverter(converters)
        .allowMainThreadQueries()
        .build()

    @Provides
    @Singleton
    fun provideJobApplicationDao(db: AppDatabase): JobApplicationDao = db.jobApplicationDao()

    @Provides
    @Singleton
    fun provideCareerInsightsDao(db: AppDatabase): CareerInsightsDao = db.careerInsightsDao()
}
