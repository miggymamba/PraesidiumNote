package com.miguelrivera.praesidiumnote.data.di

import android.content.Context
import androidx.room.Room
import com.miguelrivera.praesidiumnote.data.local.database.NoteDatabase
import com.miguelrivera.praesidiumnote.data.local.database.dao.NoteDao
import com.miguelrivera.praesidiumnote.data.local.security.PassphraseManager
import com.miguelrivera.praesidiumnote.data.repository.NoteRepositoryImpl
import com.miguelrivera.praesidiumnote.domain.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier for identifying the IO Dispatcher within the DI graph.
 * This prevents ambiguity if we add different dispatchers later.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    /**
     * Binds the repository interface to the implementation using @Binds.
     * Preferred over @Provides for constructor-injected types to optimize code generation.
     */
    @Binds
    @Singleton
    abstract fun bindNoteRepository(noteRepositoryImpl: NoteRepositoryImpl): NoteRepository

    companion object {
        /**
         * Provides the IO Dispatcher.
         * * Design Choice: Co-located here because the Database/Repository layer
         * is currently the primary consumer of IO operations.
         * This avoids creating a separate file for a single provider (YAGNI).
         */
        @IoDispatcher
        @Provides
        @Singleton
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context, passphraseManager: PassphraseManager): NoteDatabase {
            val passphrase = passphraseManager.getPassphrase()
            val factory = SupportOpenHelperFactory(passphrase)

            return Room.databaseBuilder(
                context,
                NoteDatabase::class.java,
                NoteDatabase.DATABASE_NAME
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                .also {
                    // Security Requirement: Zero out key material from heap post-init.
                    passphrase.fill(0)
                }
        }

        @Provides
        @Singleton
        fun provideNoteDao(db: NoteDatabase): NoteDao {
            return db.noteDao
        }
    }
}