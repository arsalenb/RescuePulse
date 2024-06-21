package it.unipi.RescuePulse.mobile

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PostSetupActivity : AppCompatActivity() {

    private lateinit var textName: TextView
    private lateinit var textSurname: TextView
    private lateinit var textDob: TextView
    private lateinit var textWeight: TextView
    private lateinit var textEmergencyServiceNumber: TextView
    private lateinit var emergencyContactsList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_setup)

        textName = findViewById(R.id.text_name)
        textSurname = findViewById(R.id.text_surname)
        textDob = findViewById(R.id.text_dob)
        textWeight = findViewById(R.id.text_weight)
        textEmergencyServiceNumber = findViewById(R.id.text_emergency_service_number)
        emergencyContactsList = findViewById(R.id.emergency_contacts_list)

        loadUserData()
    }

    private fun loadUserData() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        val name = sharedPreferences.getString("name", "")
        val surname = sharedPreferences.getString("surname", "")
        val dob = sharedPreferences.getString("dob", "")
        val weight = sharedPreferences.getInt("weight", 0)
        val emergencyServiceNumber = sharedPreferences.getString("emergency_service_number", "")
        val emergencyContacts = sharedPreferences.getStringSet("emergency_contacts", emptySet())

        textName.text = "Name: $name"
        textSurname.text = "Surname: $surname"
        textDob.text = "Date of Birth: $dob"
        textWeight.text = "Weight: $weight"
        textEmergencyServiceNumber.text = "Emergency Service Number: $emergencyServiceNumber"
println(emergencyContacts)
        emergencyContacts?.forEach { contact ->
            addEmergencyContactView(contact)
        }
    }

    private fun addEmergencyContactView(contactName: String) {
        val contactView = layoutInflater.inflate(R.layout.item_emergency_contact, emergencyContactsList, false)
        val contactNameTextView: TextView = contactView.findViewById(R.id.contact_name)
        contactNameTextView.text = contactName
        emergencyContactsList.addView(contactView)
    }
}
