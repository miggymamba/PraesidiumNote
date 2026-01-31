package com.miguelrivera.praesidiumnote.data.di

import com.miguelrivera.praesidiumnote.data.local.security.Pbkdf2Engine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun providePbkdf2Engine(): Pbkdf2Engine = Pbkdf2Engine()
}