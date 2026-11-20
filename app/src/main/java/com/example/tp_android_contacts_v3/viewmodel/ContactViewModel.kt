package com.example.tp_android_contacts_v3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tp_android_contacts_v3.data.ContactDao
import com.example.tp_android_contacts_v3.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = ContactDao(application)

    private val _contactsLiveData = MutableLiveData<List<Contact>>()
    val contactsLiveData: LiveData<List<Contact>> get() = _contactsLiveData

    private var currentQuery: String = ""

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                if (currentQuery.isEmpty()) {
                    dao.getAll()
                } else {
                    dao.search(currentQuery)
                }
            }
            _contactsLiveData.value = list
        }
    }

    fun setSearchQuery(query: String) {
        currentQuery = query.trim()
        loadContacts()
    }

    suspend fun getContactById(id: Long): Contact? {
        return withContext(Dispatchers.IO) {
            dao.getById(id)
        }
    }

    fun insertContact(contact: Contact, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.insert(contact)
            }
            loadContacts()
            onComplete()
        }
    }

    fun updateContact(contact: Contact, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.update(contact)
            }
            loadContacts()
            onComplete()
        }
    }

    fun deleteContact(id: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.delete(id)
            }
            loadContacts()
            onComplete()
        }
    }
}
