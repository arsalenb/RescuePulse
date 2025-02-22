package it.unipi.RescuePulse.mobile.setupFragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import it.unipi.RescuePulse.mobile.model.Contact
import it.unipi.RescuePulse.mobile.model.SharedViewModel
import androidx.fragment.app.activityViewModels
import it.unipi.RescuePulse.mobile.R

class EmergencyContactsFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var emergencyContactsList: LinearLayout
    private lateinit var buttonAddEmergencyContact: ImageButton
    private lateinit var emergencyServiceNumber: EditText

    private val pickContactLauncher = registerForActivityResult(ActivityResultContracts.PickContact()) { contactUri: Uri? ->
        contactUri ?: return@registerForActivityResult

        // Save the pending contact URI and handle it
        handlePickedContact(contactUri)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view: View = inflater.inflate(R.layout.fragment_emergency_contacts, container, false)

        emergencyContactsList = view.findViewById(R.id.emergency_contacts_list)
        buttonAddEmergencyContact = view.findViewById(R.id.button_add_emergency_contact)
        emergencyServiceNumber = view.findViewById(R.id.emergency_service_number)

        // Add existing selected contacts
        sharedViewModel.selectedContacts.observe(viewLifecycleOwner) { contacts ->
            emergencyContactsList.removeAllViews()
            contacts.forEach { contact ->
                addEmergencyContactView(emergencyContactsList, contact.displayName, contact.phoneNumber)
            }
        }

        sharedViewModel.emergencyServiceNumber.observe(viewLifecycleOwner) { number ->
            if (emergencyServiceNumber.text.toString() != number) {
                emergencyServiceNumber.setText(number)
            }
        }

        emergencyServiceNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sharedViewModel.setEmergencyServiceNumber(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        buttonAddEmergencyContact.setOnClickListener {
            pickContactLauncher.launch(null)
        }

        return view
    }

    private fun addEmergencyContactView(parentLayout: LinearLayout, contactName: String, phoneNumber: String?) {
        val contact = Contact(contactName, phoneNumber)

        // Update UI code to display contact information
        val contactView: View = layoutInflater.inflate(R.layout.item_emergency_contact, parentLayout, false)
        val contactNameTextView: TextView = contactView.findViewById(R.id.contact_name)
        val deleteButton: ImageButton = contactView.findViewById(R.id.button_delete_contact)

        contactNameTextView.text = contact.displayName
        deleteButton.setOnClickListener {
            // Remove the contact from the list and the view
            sharedViewModel.removeContact(contact)
            parentLayout.removeView(contactView)
        }

        parentLayout.addView(contactView)
    }

    private fun handlePickedContact(contactUri: Uri) {
        val (displayName, phoneNumber) = retrieveContactDetails(contactUri)
        val contact = Contact(displayName, phoneNumber)

        // Add the contact if not already present
        sharedViewModel.addContact(contact)
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
}
