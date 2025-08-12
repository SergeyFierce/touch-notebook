package com.example.otebookbeta

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otebookbeta.data.Contact
import com.example.otebookbeta.data.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    private val repository: ContactRepository
) : ViewModel() {

    private val _contact = MutableStateFlow<Contact?>(null)
    val contact: StateFlow<Contact?> = _contact

    fun loadContact(contactId: Int) {
        viewModelScope.launch {
            repository.getContactById(contactId)
                .catch { e ->
                    Log.e("ContactDetailVM", "loadContact error: ${e.message}", e)
                    emit(null)
                }
                .collect { contact -> _contact.value = contact }
        }
    }

    fun saveContact(contact: Contact) {
        viewModelScope.launch {
            try {
                if (contact.id == 0) {
                    repository.insertContact(contact)
                } else {
                    repository.updateContact(contact)
                }
                Log.d("ContactDetailViewModel", "Contact saved: $contact")
            } catch (e: Exception) {
                Log.e("ContactDetailViewModel", "Error saving contact: ${e.message}", e)
            }
        }
    }

    fun deleteContact(contactId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteContact(contactId)
                Log.d("ContactDetailViewModel", "Контакт удалён: $contactId")
            } catch (e: Exception) {
                Log.e("ContactDetailViewModel", "Ошибка при удалении контакта: ${e.message}", e)
            }
        }
    }
}
