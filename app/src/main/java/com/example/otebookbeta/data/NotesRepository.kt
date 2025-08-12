package com.example.otebookbeta.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

class NotesRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getNotesByContactId(contactId: Int): Flow<List<Note>> {
        return noteDao.getNotesByContactId(contactId)
            .catch { e ->
                Log.e("NotesRepository", "getNotesByContactId error: ${e.message}", e)
                throw RepositoryException("Не удалось загрузить заметки для контакта id=$contactId", e)
            }
    }

    fun getNoteById(noteId: Long): Flow<Note?> {
        return noteDao.getNoteById(noteId)
            .catch { e ->
                Log.e("NotesRepository", "getNoteById error: ${e.message}", e)
                throw RepositoryException("Не удалось загрузить заметку id=$noteId", e)
            }
    }

    suspend fun insertNote(note: Note) {
        try {
            noteDao.insertNote(note)
        } catch (e: Exception) {
            Log.e("NotesRepository", "insertNote error: ${e.message}", e)
            throw RepositoryException("Не удалось добавить заметку", e)
        }
    }

    suspend fun updateNote(note: Note) {
        try {
            noteDao.updateNote(note)
        } catch (e: Exception) {
            Log.e("NotesRepository", "updateNote error: ${e.message}", e)
            throw RepositoryException("Не удалось обновить заметку", e)
        }
    }

    suspend fun deleteNote(noteId: Long) {
        try {
            noteDao.deleteNote(noteId)
        } catch (e: Exception) {
            Log.e("NotesRepository", "deleteNote error: ${e.message}", e)
            throw RepositoryException("Не удалось удалить заметку", e)
        }
    }
}
