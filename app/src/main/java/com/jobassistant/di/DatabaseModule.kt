package com.jobassistant.di

import android.content.Context
import com.jobassistant.data.db.AppDatabase
import com.jobassistant.data.db.Converters
import com.jobassistant.data.db.dao.CareerInsightsDao
import com.jobassistant.data.db.dao.JobApplicationDao
import com.jobassistant.util.PassphraseManager
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
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        converters: Converters
    ): AppDatabase {
        val passphrase = PassphraseManager.getOrCreatePassphrase(context)
        return AppDatabase.create(context, passphrase, converters)
    }

    @Provides
    @Singleton
    fun provideJobApplicationDao(db: AppDatabase): JobApplicationDao = db.jobApplicationDao()

    @Provides
    @Singleton
    fun provideCareerInsightsDao(db: AppDatabase): CareerInsightsDao = db.careerInsightsDao()
}
