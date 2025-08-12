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
class NoteListViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _recentNotes = MutableStateFlow<List<Note>>(emptyList())
    val recentNotes: StateFlow<List<Note>> = _recentNotes

    fun loadNotes(contactId: Int) {
        viewModelScope.launch {
            repository.getNotesByContactId(contactId)
                .catch { e ->
                    Log.e("NoteListViewModel", "loadNotes error: ${e.message}", e)
                    emit(emptyList())
                }
                .collect { notes ->
                    _notes.value = notes
                    _recentNotes.value = notes.take(3)
                }
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteNote(noteId)
                Log.d("NoteListViewModel", "Заметка удалена: $noteId")
            } catch (e: Exception) {
                Log.e("NoteListViewModel", "Ошибка при удалении заметки: ${e.message}", e)
            }
        }
    }
}
