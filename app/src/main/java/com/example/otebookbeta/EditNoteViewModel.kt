package com.example.otebookbeta

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otebookbeta.data.Note
import com.example.otebookbeta.data.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note

    fun loadNote(noteId: Long) {
        viewModelScope.launch {
            try {
                repository.getNoteById(noteId)
                    .catch { e ->
                        Log.e("EditNoteViewModel", "loadNote error: ${e.message}", e)
                        emit(null)
                    }
                    .collect { note -> _note.value = note }
            } catch (e: Exception) {
                Log.e("EditNoteViewModel", "collect error: ${e.message}", e)
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.updateNote(note)
                Log.d("EditNoteViewModel", "Заметка обновлена: $note")
            } catch (e: Exception) {
                Log.e("EditNoteViewModel", "Ошибка при обновлении заметки: ${e.message}", e)
            }
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteNote(noteId)
                Log.d("EditNoteViewModel", "Заметка удалена: $noteId")
            } catch (e: Exception) {
                Log.e("EditNoteViewModel", "Ошибка при удалении заметки: ${e.message}", e)
            }
        }
    }
}
