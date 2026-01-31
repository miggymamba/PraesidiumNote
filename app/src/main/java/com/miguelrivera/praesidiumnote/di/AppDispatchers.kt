package com.miguelrivera.praesidiumnote.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Dispatcher(val dispatcher: PraesidiumDispatchers)
enum class PraesidiumDispatchers {
    /**
     * Optimized for CPU-intensive work.
     * Use this for sorting lists, parsing JSON, or complex calculations.
     * Backed by a thread pool roughly equal to the number of CPU cores.
     */
    Default,

    /**
     * Optimized for blocking I/O operations.
     * Use this for Network requests (Retrofit), Database queries (Room), or File read/writes.
     * Backed by a large thread pool (64+) to handle waiting threads efficiently.
     */
    IO
}