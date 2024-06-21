package it.unipi.RescuePulse.mobile

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class EmergencyContactsFragment : Fragment() {

    private val selectedContacts = mutableListOf<String>()
    private lateinit var emergencyContactsList: LinearLayout
    private lateinit var buttonAddEmergencyContact: ImageButton
    private lateinit var emergencyServiceNumber: EditText

    // ActivityResultLauncher to pick a contact
    private val pickContactLauncher = registerForActivityResult(ActivityResultContracts.PickContact()) { contactUri ->
        contactUri ?: return@registerForActivityResult
        // Handle contact selection (e.g., retrieve contact name and add to selectedContacts)
        val contactName = retrieveContactName(contactUri)
        if (!selectedContacts.contains(contactName)) {
            selectedContacts.add(contactName)
            addEmergencyContactView(emergencyContactsList, contactName)
            saveEmergencyContacts()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_emergency_contacts, container, false)

        emergencyContactsList = view.findViewById(R.id.emergency_contacts_list)
        buttonAddEmergencyContact = view.findViewById(R.id.button_add_emergency_contact)
        emergencyServiceNumber = view.findViewById(R.id.emergency_service_number)

        // Add existing selected contacts (placeholder)
        selectedContacts.forEach { contactName ->
            addEmergencyContactView(emergencyContactsList, contactName)
        }

        buttonAddEmergencyContact.setOnClickListener {
            pickContactLauncher.launch(null)
        }

        loadEmergencyContacts()
        loadEmergencyServiceNumber()

        return view
    }

    private fun addEmergencyContactView(parentLayout: LinearLayout, contactName: String) {
        val contactView = layoutInflater.inflate(R.layout.item_emergency_contact, parentLayout, false)
        val contactNameTextView: TextView = contactView.findViewById(R.id.contact_name)
        val deleteButton: ImageButton = contactView.findViewById(R.id.button_delete_contact)

        contactNameTextView.text = contactName
        deleteButton.setOnClickListener {
            // Remove the contact from the list and the view
            selectedContacts.remove(contactName)
            parentLayout.removeView(contactView)
            saveEmergencyContacts()

        }

        parentLayout.addView(contactView)
    }

    private fun retrieveContactName(contactUri: Uri): String {
        val cursor = requireActivity().contentResolver.query(contactUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                return it.getString(nameIndex)
            }
        }
        return ""
    }

    private fun saveEmergencyContacts() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("emergency_contacts", selectedContacts.toSet())
        editor.apply()
    }

    private fun loadEmergencyContacts() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
        val contacts = sharedPreferences.getStringSet("emergency_contacts", emptySet()) ?: emptySet()
        selectedContacts.clear()
        selectedContacts.addAll(contacts)
        selectedContacts.forEach { addEmergencyContactView(emergencyContactsList, it) }
    }
    private fun saveEmergencyServiceNumber() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("emergency_service_number", emergencyServiceNumber.text.toString())
        editor.apply()
    }

    private fun loadEmergencyServiceNumber() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
        val serviceNumber = sharedPreferences.getString("emergency_service_number", "")
        emergencyServiceNumber.setText(serviceNumber)
    }

    override fun onPause() {
        super.onPause()
        saveEmergencyServiceNumber()
    }
}
