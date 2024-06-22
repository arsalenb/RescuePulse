package it.unipi.RescuePulse.mobile.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.unipi.RescuePulse.mobile.model.Contact

class SharedViewModel : ViewModel() {
    private val _selectedContacts = MutableLiveData<MutableList<Contact>>(mutableListOf())
    val selectedContacts: LiveData<MutableList<Contact>> get() = _selectedContacts

    private val _emergencyServiceNumber = MutableLiveData<String>()
    val emergencyServiceNumber: LiveData<String> get() = _emergencyServiceNumber

    fun addContact(contact: Contact) {
        _selectedContacts.value?.add(contact)
        _selectedContacts.value = _selectedContacts.value // Trigger observers
    }

    fun removeContact(contact: Contact) {
        _selectedContacts.value?.remove(contact)
        _selectedContacts.value = _selectedContacts.value // Trigger observers
    }

    fun setEmergencyServiceNumber(number: String) {
        _emergencyServiceNumber.value = number
    }
}
