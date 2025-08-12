package com.example.otebookbeta

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otebookbeta.data.Contact
import com.example.otebookbeta.data.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddContactViewModel @Inject constructor(
    private val repository: ContactRepository
) : ViewModel() {

    fun saveContact(contact: Contact) {
        viewModelScope.launch {
            try {
                Log.d("AddContactViewModel", "Saving contact: $contact")
                repository.insertContact(contact)
                Log.d("AddContactViewModel", "Contact saved successfully")
            } catch (e: Exception) {
                Log.e("AddContactViewModel", "Error saving contact: ${e.message}", e)
            }
        }
    }
}