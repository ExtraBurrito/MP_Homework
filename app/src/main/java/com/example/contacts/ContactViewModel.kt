package com.example.contacts
import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class ContactViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).contactDao()
    val allContacts: LiveData<List<Contact>> = dao.getAll().asLiveData()

    fun addContact(contact: Contact) = viewModelScope.launch {
        dao.insert(contact)
    }

    fun updateContact(contact: Contact) = viewModelScope.launch {
        dao.update(contact)
    }

    fun deleteContact(contact: Contact) = viewModelScope.launch {
        dao.delete(contact)
    }

    suspend fun getContactById(id: Int): Contact? {
        return dao.getContactById(id)
    }

}