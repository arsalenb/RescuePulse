package it.unipi.RescuePulse.mobile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import it.unipi.RescuePulse.mobile.model.Contact
import it.unipi.RescuePulse.mobile.service.EmergencyNotificationService
import it.unipi.RescuePulse.mobile.utils.DateUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PostSetupActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {

    private lateinit var settingsButton: ImageButton
    private lateinit var emergencyContactsList: LinearLayout
    private lateinit var emergencyServiceNumber: TextView
    private lateinit var messageClient: MessageClient
    private lateinit var statusText: TextView
    private lateinit var statusIcon: ImageView
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_setup)

        settingsButton = findViewById(R.id.settings_button)
        emergencyContactsList = findViewById(R.id.emergency_contacts_list)
        emergencyServiceNumber = findViewById(R.id.emergency_service_number)
        statusText = findViewById(R.id.status_text)
        statusIcon = findViewById(R.id.status_icon)

        settingsButton.setOnClickListener {
            val intent = Intent(this, SetupActivity::class.java)
            startActivity(intent)
        }

        messageClient = Wearable.getMessageClient(this)
        Wearable.getMessageClient(this).addListener(this)

        // Load contacts and emergency service number from SharedPreferences
        loadEmergencyContacts()
        loadEmergencyServiceNumber()

        // Send initiation and age to wearable (if any)
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                sendInitiateSignalToWearable()
                handler.postDelayed(this, 60000)  // Check every 10 seconds
            }
        }
        handler.post(runnable)
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

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            "/acknowledgment" -> {
                val message = String(messageEvent.data)
                Log.d("PostSetupActivity", "Received acknowledgment: $message")

                // Update the UI to show the connection status
                statusText.text = getString(R.string.status_listening_emergency)
                statusText.setTextColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.holo_green_dark
                    ))
                statusIcon.setImageResource(R.drawable.ic_pending)

                // Stop sending the initiation message
                handler.removeCallbacks(runnable)

                // Start emergency notification service listener
                val serviceIntent = Intent(this, EmergencyNotificationService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }

            }
        }
    }

    private fun sendInitiateSignalToWearable() {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val dobString = sharedPreferences.getString("dob", "")

        // Calculate age using the DateUtils function
        val age = DateUtils.calculateAgeFromDob(dobString)

        if (age == null) {
            Log.e("sendInitiateSignalToWearable", "DOB is empty or invalid")
            return
        }

        val message = "initiate:$age"

        Wearable.getNodeClient(this).connectedNodes
            .addOnCompleteListener { task: Task<List<Node>> ->
                if (task.isSuccessful) {
                    val nodes = task.result
                    for (node in nodes) {
                        messageClient.sendMessage(node.id, "/initiate", message.toByteArray())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("PostSetupActivity", "Initiate signal sent successfully to ${node.displayName}")
                                } else {
                                    Log.e("PostSetupActivity", "Failed to send initiate signal to ${node.displayName}")
                                }
                            }
                    }
                } else {
                    Log.e("PostSetupActivity", "Failed to get connected nodes.")
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getMessageClient(this).removeListener(this)

        // Stop the emergency notification service
        Log.d("PostSetupActivity", "emergency notification service is destroyed")

        val intent = Intent(this, EmergencyNotificationService::class.java)
        stopService(intent)
    }

}
