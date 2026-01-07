package com.miguelrivera.praesidiumnote.data.di

import com.miguelrivera.praesidiumnote.data.local.security.Pbkdf2Engine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier for identifying the Default Dispatcher within the DI graph.
 * Prevents ambiguity with other dispatcher types (IO/Main).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    /**
     * Provides the Default Dispatcher.
     * * Co-located here as the security layer is currently the primary consumer
     * of heavy CPU operations, avoiding unnecessary file fragmentation.
     */
    @DefaultDispatcher
    @Provides
    @Singleton
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    fun providePbkdf2Engine(): Pbkdf2Engine = Pbkdf2Engine()
}