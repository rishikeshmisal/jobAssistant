package com.jobassistant.di

import com.jobassistant.data.repository.ClaudeRepository
import com.jobassistant.data.repository.GeminiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds
    @Singleton
    abstract fun bindClaudeRepository(impl: GeminiRepository): ClaudeRepository
}
