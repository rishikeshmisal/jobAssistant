package com.jobassistant.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.jobassistant.data.repository.CareerInsightsRepositoryImpl
import com.jobassistant.data.repository.JobApplicationRepositoryImpl
import com.jobassistant.data.repository.UserProfileDataStore
import com.jobassistant.domain.repository.CareerInsightsRepository
import com.jobassistant.domain.repository.JobApplicationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_profile"
)

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindJobApplicationRepository(
        impl: JobApplicationRepositoryImpl
    ): JobApplicationRepository

    @Binds
    @Singleton
    abstract fun bindCareerInsightsRepository(
        impl: CareerInsightsRepositoryImpl
    ): CareerInsightsRepository

    companion object {

        @Provides
        @Singleton
        fun provideUserProfileDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> = context.userPreferencesDataStore

        @Provides
        @Singleton
        fun provideUserProfileDataStoreWrapper(
            dataStore: DataStore<Preferences>
        ): UserProfileDataStore = UserProfileDataStore(dataStore)
    }
}
