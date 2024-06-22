package it.unipi.RescuePulse.mobile.model

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.unipi.RescuePulse.mobile.model.Contact

class SharedViewModel : ViewModel() {
    private val _selectedContacts = MutableLiveData<MutableList<Contact>>(mutableListOf())
    val selectedContacts: LiveData<MutableList<Contact>> get() = _selectedContacts

    private val _emergencyServiceNumber = MutableLiveData<String>()
    val emergencyServiceNumber: LiveData<String> get() = _emergencyServiceNumber

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> get() = _name

    private val _surname = MutableLiveData<String>()
    val surname: LiveData<String> get() = _surname

    private val _dob = MutableLiveData<String>()
    val dob: LiveData<String> get() = _dob

    private val _weight = MutableLiveData<Int>()
    val weight: LiveData<Int> get() = _weight



    private lateinit var sharedPreferences: SharedPreferences

    fun initSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        loadContacts()
        loadEmergencyServiceNumber()
        loadPersonalInformation()
    }

    private fun loadPersonalInformation() {
        _name.value = sharedPreferences.getString("name", "")
        _surname.value = sharedPreferences.getString("surname", "")
        _dob.value = sharedPreferences.getString("dob", "")
        _weight.value = sharedPreferences.getInt("weight", 0)
    }

    fun savePersonalInformation() {
        sharedPreferences.edit().apply {
            putString("name", _name.value)
            putString("surname", _surname.value)
            putString("dob", _dob.value)
            _weight.value?.let { putInt("weight", it) }
            apply()
        }

    }

    fun setName(name: String) {
        _name.value = name
    }

    fun setSurname(surname: String) {
        _surname.value = surname
    }

    fun setDob(dob: String) {
        _dob.value = dob
    }

    fun setWeight(weight: Int) {
        _weight.value = weight
    }
    private fun loadContacts() {
        val contactsJson = sharedPreferences.getString("emergency_contacts", null)
        _selectedContacts.value = Contact.fromJsonList(contactsJson)
    }

    fun saveContacts() {
        val contactsJson = Contact.toJsonList(_selectedContacts.value ?: mutableListOf())
        sharedPreferences.edit().putString("emergency_contacts", contactsJson).apply()
    }

    private fun loadEmergencyServiceNumber() {
        _emergencyServiceNumber.value = sharedPreferences.getString("emergency_service_number", "")
    }

    fun saveEmergencyServiceNumber() {
        sharedPreferences.edit().putString("emergency_service_number", _emergencyServiceNumber.value).apply()
    }

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
