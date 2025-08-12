package com.example.otebookbeta.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.otebookbeta.data.AppDatabase
import com.example.otebookbeta.data.ContactDao
import com.example.otebookbeta.data.ContactRepository
import com.example.otebookbeta.data.NoteDao
import com.example.otebookbeta.data.NotesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        Log.d("AppModule", "Creating Room database")
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "otebook_database"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()
    }

    @Provides
    @Singleton
    fun provideContactDao(database: AppDatabase): ContactDao = database.contactDao()

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    fun provideContactRepository(contactDao: ContactDao): ContactRepository =
        ContactRepository(contactDao)

    @Provides
    @Singleton
    fun provideNotesRepository(noteDao: NoteDao): NotesRepository =
        NotesRepository(noteDao)
}
