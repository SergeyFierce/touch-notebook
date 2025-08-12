package com.example.otebookbeta

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otebookbeta.data.Contact
import com.example.otebookbeta.data.ContactRepository
import com.example.otebookbeta.utils.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val repository: ContactRepository
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> get() = _contacts

    private val _categoryTitle = MutableStateFlow("")
    val categoryTitle: StateFlow<String> get() = _categoryTitle

    private val _contactsCountText = MutableStateFlow("")
    val contactsCountText: StateFlow<String> get() = _contactsCountText

    fun loadContacts(category: String) {
        _categoryTitle.value = category
        viewModelScope.launch {
            Log.d("ContactListViewModel", "Loading contacts for category: $category")
            repository.getContactsByCategory(category)
                .catch { e ->
                    Log.e("ContactListViewModel", "Flow error: ${e.message}", e)
                    emit(emptyList())
                }
                .collect { contactList ->
                    Log.d("ContactListViewModel", "Received ${contactList.size} contacts")
                    _contacts.value = contactList

                    val count = contactList.size
                    val word = StringUtils.getCategoryWord(count, category)
                    _contactsCountText.value = "$count $word"
                }
        }
    }

    fun deleteContact(contactId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteContact(contactId)
            } catch (e: Exception) {
                Log.e("ContactListViewModel", "deleteContact: ${e.message}", e)
            }
        }
    }
}
