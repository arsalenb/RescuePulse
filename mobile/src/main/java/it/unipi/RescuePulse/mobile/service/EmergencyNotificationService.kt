package it.unipi.RescuePulse.mobile.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import it.unipi.RescuePulse.mobile.PostSetupActivity
import it.unipi.RescuePulse.mobile.R
import it.unipi.RescuePulse.mobile.model.Contact
import it.unipi.RescuePulse.mobile.utils.DateUtils

class EmergencyNotificationService : Service(), MessageClient.OnMessageReceivedListener {

    private var lastNotificationTime: Long = 0
    private val notificationInterval = 5 * 60 * 1000 // 5 minutes in milliseconds

    override fun onCreate() {
        super.onCreate()
        Log.d("EmergencyService", "Service created")
        Wearable.getMessageClient(this).addListener(this)
        startForegroundService()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("EmergencyService", "Service destroyed")
        Wearable.getMessageClient(this).removeListener(this)
    }

    private fun startForegroundService() {
        val channelId = "emergency_notifications"
        val channelName = "Emergency Notifications"

        // Create notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("RescuePulse Service")
            .setContentText("Listening for emergency notifications...")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Start service in foreground with the notification
        startForeground(100, notification)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("EmergencyService", "Message received with path: ${messageEvent.path}")
        if (messageEvent.path == "/emergency") {
            val message = String(messageEvent.data)

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastNotificationTime < notificationInterval) {
                Log.d("EmergencyService", "Ignoring notification within interval")
                return
            }
            lastNotificationTime = currentTime

            handleEmergencyNotification(message)
        }
    }

    private fun handleEmergencyNotification(message: String) {
        val data = message.split(";")
        val status = data[0].split(":")[1]
        val timestamp = data[1].split(":")[1].toLong()
        val latitude = data[2].split(":")[1].toDouble()
        val longitude = data[3].split(":")[1].toDouble()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "emergency_notifications"
        val channelName = "Emergency Notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, PostSetupActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this).apply {
            addNextIntentWithParentStack(notificationIntent)
        }
        val notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Emergency Alert")
            .setContentText("Status: $status, Timestamp: $timestamp, Location: ($latitude, $longitude)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(notificationPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
        Log.d("EmergencyService", "Notification shown")

        initiateEmergencyCall()
        sendEmergencySms(status, latitude, longitude)
    }

    private fun sendEmergencySms(status: String, latitude: Double, longitude: Double) {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val contactsJson = sharedPreferences.getString("emergency_contacts", null)
        val contacts = Contact.fromJsonList(contactsJson)

        val firstName = sharedPreferences.getString("name", "Unknown")
        val lastName = sharedPreferences.getString("surname", "Unknown")
        val dobString = sharedPreferences.getString("dob", "")

        val ageString = DateUtils.calculateAgeFromDob(dobString)?.toString() ?: "Unknown"
        val messageTemplate = "Emergency with Critical Heart Situation! Victim: $firstName $lastName, Age: $ageString. Heart Issue: $status. Location: https://maps.google.com/?q=$latitude,$longitude"
        val smsManager = android.telephony.SmsManager.getDefault()

        contacts.forEach { contact ->
            smsManager.sendTextMessage(contact.phoneNumber, null, messageTemplate, null, null)
            Log.d("EmergencyService", "SMS sent to ${contact.phoneNumber}")
        }
    }

    private fun initiateEmergencyCall() {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val emergencyNumber = sharedPreferences.getString("emergency_service_number", "") ?: ""

        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        val phoneAccountHandle = PhoneAccountHandle(
            ComponentName(this, EmergencyCallService::class.java),
            "emergency_call_account"
        )

        val phoneAccount = PhoneAccount.builder(phoneAccountHandle, "Emergency Call Account")
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            .build()

        telecomManager.registerPhoneAccount(phoneAccount)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_OWN_CALLS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            telecomManager.placeCall(Uri.parse("tel:$emergencyNumber"), null)
            Log.d("EmergencyService", "Call initiated to $emergencyNumber")
        } else {
            Log.e("EmergencyService", "Manage own calls or Call phone permission not granted")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
