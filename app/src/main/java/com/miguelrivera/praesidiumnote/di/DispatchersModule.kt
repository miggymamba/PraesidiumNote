package com.miguelrivera.praesidiumnote.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Hilt module that maps the [PraesidiumDispatchers] enum keys to the actual [CoroutineDispatcher] implementations.
 *
 * This abstraction allows us to swap these dispatchers for [TestDispatcher]s during Unit/Integration tests,
 * ensuring tests run deterministically without race conditions.
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @Dispatcher(PraesidiumDispatchers.IO)
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(PraesidiumDispatchers.Default)
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}