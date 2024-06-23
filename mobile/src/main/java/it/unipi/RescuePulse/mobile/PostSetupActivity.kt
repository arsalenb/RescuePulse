package it.unipi.RescuePulse.mobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import it.unipi.RescuePulse.mobile.model.Contact

class PostSetupActivity : AppCompatActivity() {

    private lateinit var settingsButton: ImageButton
    private lateinit var emergencyContactsList: LinearLayout
    private lateinit var emergencyServiceNumber: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_setup)

        settingsButton = findViewById(R.id.settings_button)
        emergencyContactsList = findViewById(R.id.emergency_contacts_list)
        emergencyServiceNumber = findViewById(R.id.emergency_service_number)

        settingsButton.setOnClickListener {
            val intent = Intent(this, SetupActivity::class.java)
            startActivity(intent)
        }

        // Load contacts and emergency service number from SharedPreferences
        loadEmergencyContacts()
        loadEmergencyServiceNumber()
    }

    private fun loadEmergencyContacts() {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val contactsJson = sharedPreferences.getString("emergency_contacts", null)
        val contacts = Contact.fromJsonList(contactsJson)

        emergencyContactsList.removeAllViews()
        contacts.forEach { contact ->
            addEmergencyContactView(emergencyContactsList, contact.displayName, contact.phoneNumber)
        }
    }

    private fun loadEmergencyServiceNumber() {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val emergencyNumber = sharedPreferences.getString("emergency_service_number", "")
        emergencyServiceNumber.text = emergencyNumber
    }

    private fun addEmergencyContactView(parentLayout: LinearLayout, contactName: String, phoneNumber: String?) {
        val contactView = layoutInflater.inflate(R.layout.post_setup_contact, parentLayout, false)
        val contactNameTextView: TextView = contactView.findViewById(R.id.contact_full_display)
        val displayText = getString(R.string.contact_display_format, contactName, phoneNumber)
        contactNameTextView.text = displayText
        parentLayout.addView(contactView)
    }
}
