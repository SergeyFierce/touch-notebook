package com.example.otebookbeta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otebookbeta.data.Note
import com.example.otebookbeta.data.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    fun saveNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.insertNote(note)
            } catch (e: Exception) {
                // Логирование ошибки
                android.util.Log.e("AddNoteViewModel", "Ошибка при сохранении заметки: ${e.message}", e)
            }
        }
    }
}