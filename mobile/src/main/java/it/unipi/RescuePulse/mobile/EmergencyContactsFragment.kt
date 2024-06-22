package it.unipi.RescuePulse.mobile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import it.unipi.RescuePulse.mobile.model.Contact

class EmergencyContactsFragment : Fragment() {

    private val selectedContacts: MutableList<Contact> = mutableListOf()
    private lateinit var emergencyContactsList: LinearLayout
    private lateinit var buttonAddEmergencyContact: ImageButton
    private lateinit var emergencyServiceNumber: EditText

    private var pendingContactUri: Uri? = null

    private val pickContactLauncher = registerForActivityResult(ActivityResultContracts.PickContact()) { contactUri: Uri? ->
        contactUri ?: return@registerForActivityResult

        // Save the pending contact URI and request permission
        pendingContactUri = contactUri
        if (isReadContactsPermissionGranted()) {
            handlePickedContact(contactUri)
        } else {
            requestReadContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private val requestReadContactsPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, handle the picked contact
            pendingContactUri?.let { handlePickedContact(it) }
        } else {
            // Permission denied, handle this scenario
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_emergency_contacts, container, false)

        emergencyContactsList = view.findViewById(R.id.emergency_contacts_list)
        buttonAddEmergencyContact = view.findViewById(R.id.button_add_emergency_contact)
        emergencyServiceNumber = view.findViewById(R.id.emergency_service_number)

        // Add existing selected contacts (placeholder)
        selectedContacts.forEach { contact ->
            addEmergencyContactView(emergencyContactsList, contact.displayName, contact.phoneNumber)
        }

        buttonAddEmergencyContact.setOnClickListener {
            if (isReadContactsPermissionGranted()) {
                pickContactLauncher.launch(null)
            } else {
                requestReadContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }

        loadEmergencyContacts()
        loadEmergencyServiceNumber()

        return view
    }

    private fun addEmergencyContactView(parentLayout: LinearLayout, contactName: String, phoneNumber: String?) {
        val contact = Contact(contactName, phoneNumber)
        selectedContacts.add(contact)

        // Update UI code to display contact information
        val contactView: View = layoutInflater.inflate(R.layout.item_emergency_contact, parentLayout, false)
        val contactNameTextView: TextView = contactView.findViewById(R.id.contact_name)
        val deleteButton: ImageButton = contactView.findViewById(R.id.button_delete_contact)

        contactNameTextView.text = contact.displayName
        deleteButton.setOnClickListener {
            // Remove the contact from the list and the view
            selectedContacts.remove(contact)
            parentLayout.removeView(contactView)
            saveEmergencyContacts()
        }

        parentLayout.addView(contactView)
    }

    private fun handlePickedContact(contactUri: Uri) {
        val (displayName, phoneNumber) = retrieveContactDetails(contactUri)
        val contact = Contact(displayName, phoneNumber)

        // Add the contact if not already present
        if (!selectedContacts.contains(contact)) {
            selectedContacts.add(contact)
            addEmergencyContactView(emergencyContactsList, contact.displayName, contact.phoneNumber)
            saveEmergencyContacts()
        }
    }

    @SuppressLint("Range")
    private fun retrieveContactDetails(contactUri: Uri): Pair<String, String?> {
        var displayName: String = ""
        var phoneNumber: String? = null

        val cursor = requireActivity().contentResolver.query(contactUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex: Int = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                displayName = it.getString(nameIndex)

                val contactId: String = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                val phoneCursor = requireActivity().contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(contactId),
                    null
                )

                phoneCursor?.use { phones ->
                    if (phones.moveToFirst()) {
                        phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    }
                }
            }
        }

        return Pair(displayName, phoneNumber)
    }

    private fun isReadContactsPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    private fun saveEmergencyContacts() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("emergency_contacts", selectedContacts.map { it.toString() }.toSet())
        editor.apply()
    }

    private fun loadEmergencyContacts() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
        val contacts: Set<String> = sharedPreferences.getStringSet("emergency_contacts", emptySet()) ?: emptySet()
        selectedContacts.clear()
        selectedContacts.addAll(contacts.map { Contact.fromString(it) })
        selectedContacts.forEach { addEmergencyContactView(emergencyContactsList, it.displayName, it.phoneNumber) }
    }

    private fun saveEmergencyServiceNumber() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("emergency_service_number", emergencyServiceNumber.text.toString())
        editor.apply()
    }

    private fun loadEmergencyServiceNumber() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
        val serviceNumber: String? = sharedPreferences.getString("emergency_service_number", "")
        emergencyServiceNumber.setText(serviceNumber)
    }

    override fun onPause() {
        super.onPause()
        saveEmergencyServiceNumber()
    }
}
