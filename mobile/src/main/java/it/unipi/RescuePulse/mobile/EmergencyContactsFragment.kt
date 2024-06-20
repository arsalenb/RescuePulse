package it.unipi.RescuePulse.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class EmergencyContactsFragment : Fragment() {

    private val selectedContacts = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_emergency_contacts, container, false)

        val emergencyContactsList: LinearLayout = view.findViewById(R.id.emergency_contacts_list)
        val buttonAddEmergencyContact: ImageButton = view.findViewById(R.id.button_add_emergency_contact)

        // Add existing selected contacts (placeholder)
        selectedContacts.forEach { contactName ->
            addEmergencyContactView(emergencyContactsList, contactName)
        }

        buttonAddEmergencyContact.setOnClickListener {
            val newContactName = "New Contact ${selectedContacts.size + 1}"
            selectedContacts.add(newContactName)
            addEmergencyContactView(emergencyContactsList, newContactName)
        }

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
        }

        parentLayout.addView(contactView)
    }
}
