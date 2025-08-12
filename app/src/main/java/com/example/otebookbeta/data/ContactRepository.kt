package com.example.otebookbeta.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

class ContactRepository @Inject constructor(
    private val contactDao: ContactDao
) {

    fun getContactsByCategory(category: String): Flow<List<Contact>> {
        return contactDao.getContactsByCategory(category)
            .catch { e ->
                Log.e("ContactRepository", "getContactsByCategory error: ${e.message}", e)
                throw RepositoryException("Не удалось загрузить контакты категории: $category", e)
            }
            .onCompletion { cause ->
                if (cause == null) Log.d("ContactRepository", "getContactsByCategory completed")
            }
    }

    fun getContactById(contactId: Int): Flow<Contact?> {
        return contactDao.getContactById(contactId)
            .catch { e ->
                Log.e("ContactRepository", "getContactById error: ${e.message}", e)
                throw RepositoryException("Не удалось загрузить контакт id=$contactId", e)
            }
    }

    fun getContactCountByCategory(category: String): Flow<Int> {
        return contactDao.getContactCountByCategory(category)
            .catch { e ->
                Log.e("ContactRepository", "getContactCountByCategory error: ${e.message}", e)
                throw RepositoryException("Не удалось получить счётчик для категории: $category", e)
            }
    }

    suspend fun insertContact(contact: Contact) {
        try {
            contactDao.insertContact(contact)
        } catch (e: Exception) {
            Log.e("ContactRepository", "insertContact error: ${e.message}", e)
            throw RepositoryException("Не удалось добавить контакт", e)
        }
    }

    suspend fun updateContact(contact: Contact) {
        try {
            contactDao.updateContact(contact)
        } catch (e: Exception) {
            Log.e("ContactRepository", "updateContact error: ${e.message}", e)
            throw RepositoryException("Не удалось обновить контакт", e)
        }
    }

    suspend fun deleteContact(contactId: Int) {
        try {
            contactDao.deleteContact(contactId)
        } catch (e: Exception) {
            Log.e("ContactRepository", "deleteContact error: ${e.message}", e)
            throw RepositoryException("Не удалось удалить контакт", e)
        }
    }
}
