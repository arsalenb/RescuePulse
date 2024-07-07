package it.unipi.RescuePulse.wear.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import it.unipi.RescuePulse.wear.R
import it.unipi.RescuePulse.wear.presentation.WearMainActivity
import it.unipi.RescuePulse.wear.utils.HeartRateUtils
import it.unipi.RescuePulse.wear.utils.HeartRateUtils.HeartRateStatus



class HeartRateService : Service(), SensorEventListener, MessageClient.OnMessageReceivedListener {


    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var messageClient: MessageClient

    private var isInitiated = false
    private var userAge: Int? = null

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        messageClient = Wearable.getMessageClient(this)
        Wearable.getMessageClient(this).addListener(this)

        startForegroundService()
        sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        messageClient = Wearable.getMessageClient(this)

        // Reset initiation flag
        isInitiated = false
    }

    private fun startForegroundService() {
        val channelId = "heart_rate_monitor"
        val channelName = "Heart Rate Monitor"
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Heart Rate Monitor")
            .setContentText("Monitoring heart rate...")
            .setSmallIcon(R.drawable.ic_app_logo)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onSensorChanged(event: SensorEvent) {

        val vibrator = ContextCompat.getSystemService(this, Vibrator::class.java)

        if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
            val heartRate = event.values[0].toInt()

            // Store heart rate data
            HeartRateUtils.storeHeartRateData(this, heartRate)

            val age = userAge
            val heartRateStatus = HeartRateUtils.detectSustainedHeartRate(this, 10000, 10, 80, age)  // Adjust thresholds if necessary

            // Send broadcast with heart rate data
            broadcastHeartRateUpdate(heartRate,heartRateStatus)

            if ( heartRateStatus == HeartRateStatus.BRADYCARDIA || heartRateStatus == HeartRateStatus.TACHYCARDIA){
                if (isInitiated) {
                    sendEmergencyNotification(heartRateStatus)
                } else {
                    Log.d("HeartRateService", "Initiation flag not received yet, not sending emergency notification.")
                }
                mediaPlayer.start()
                if (vibrator != null && vibrator.hasVibrator())
                    vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
            }
            else {
                // Normal State: Stop audio alarm if it's playing
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    mediaPlayer.seekTo(0)
                }
            }
            Log.d("HeartRateService", "Heart rate: $heartRate")

        }

    }
    private fun sendEmergencyNotification(status: HeartRateStatus) {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, fineLocationPermission) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val messageData = "status:${status.name};timestamp:${System.currentTimeMillis()};latitude:${location.latitude};longitude:${location.longitude}"

                        Wearable.getNodeClient(this).connectedNodes
                            .addOnCompleteListener { task: Task<List<Node>> ->
                                if (task.isSuccessful) {
                                    val nodes = task.result
                                    for (node in nodes) {
                                        sendMessage(node.id, "/emergency", messageData)
                                    }
                                } else {
                                    Log.e("HeartRateService", "Failed to get connected nodes.")
                                }
                            }
                    } else {
                        Log.e("HeartRateService", "Last known location is null.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HeartRateService", "Failed to get location: ${e.message}", e)
                }
        } else {
            showPermissionNotification()
        }
    }

    private fun showPermissionNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannelId = "emergency_notification_channel"
        val channel = NotificationChannel(
            notificationChannelId,
            "Emergency Notification",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for emergency notifications"
        }
        notificationManager.createNotificationChannel(channel)

        // Create an intent that will open the settings page to request permissions
        val intent = Intent(this, WearMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle("Permission Needed")
            .setContentText("RescuePulse needs location permissions to send emergency notifications.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
    private fun sendMessage(nodeId: String, path: String, message: String) {
        val sendMessageTask = messageClient.sendMessage(nodeId, path, message.toByteArray())

        sendMessageTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("WearMainActivity", "Message sent successfully to node $nodeId")
            } else {
                Log.e("WearMainActivity", "Message failed to send to node $nodeId")
            }
        }
    }
    private fun broadcastHeartRateUpdate(heartRate: Int, heartRateStatus: HeartRateStatus) {
        val intent = Intent("HeartRateUpdate").apply {
            putExtra("heartRate", heartRate)
            putExtra("heartRateStatus", heartRateStatus)
        }
        sendBroadcast(intent)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/initiate") {
            val message = String(messageEvent.data)
            Log.d("WearMainActivity", "Received Initiation Flag: $message")

            // Extract the age from the message (assuming the format is "initiate:age")
            val parts = message.split(":")
            if (parts.size == 2 && parts[0] == "initiate") {
                userAge = parts[1].toIntOrNull()
                if (userAge != null) {
                    Log.d("HeartRateService", "Age extracted: $userAge")
                    isInitiated = true
                } else {
                    Log.e("HeartRateService", "Invalid age format in initiation message")
                }
            }

            isInitiated = true

            // Broadcast initiation flag received
            val intent = Intent("InitiationFlagReceived")
            sendBroadcast(intent)

            // Send acknowledgment back to the mobile app
            Wearable.getNodeClient(this).connectedNodes
                .addOnCompleteListener { task: Task<List<Node>> ->
                    if (task.isSuccessful) {
                        val nodes = task.result
                        for (node in nodes) {
                            sendMessage(node.id, "/acknowledgment", "acknowledged")
                        }
                    } else {
                        Log.e("HeartRateService", "Failed to get connected nodes.")
                    }
                }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes
    }

}
