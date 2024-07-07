package it.unipi.RescuePulse.wear.presentation

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import it.unipi.RescuePulse.wear.R
import it.unipi.RescuePulse.wear.service.HeartRateService
import it.unipi.RescuePulse.wear.utils.HeartRateUtils


class WearMainActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null

    private lateinit var heartRateText: TextView
    private lateinit var heartRateState: TextView
    private lateinit var nodeStatusText: TextView

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear_main)

        heartRateText = findViewById(R.id.heart_rate_text)
        heartRateState = findViewById(R.id.heart_rate_state)
        nodeStatusText = findViewById(R.id.node_status_text)

        mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        if (heartRateSensor == null) {
            heartRateText.text = "Heart rate sensor not available."
        } else {
            requestPermissions()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    private fun requestPermissions() {
        val bodySensorsPermission = Manifest.permission.BODY_SENSORS
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION

        if (ContextCompat.checkSelfPermission(this, bodySensorsPermission) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, fineLocationPermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(bodySensorsPermission, fineLocationPermission),
                PERMISSION_REQUEST_CODE
            )
        } else {
            startHeartRateService()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startHeartRateService()
            } else {
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permissions Required")
        builder.setMessage("This app requires location and sensor permissions to function properly. Please grant the required permissions.")
        builder.setPositiveButton("Grant Permissions") { _, _ ->
            requestPermissions()
        }
        builder.setNegativeButton("Exit App") { _, _ ->
            finish()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun startHeartRateService() {
        val serviceIntent = Intent(this, HeartRateService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    private fun setHeartRateState(state: HeartRateUtils.HeartRateStatus) {
        val stateText = "State: "
        val stateValue: String
        val stateColor: Int

        when (state) {
            HeartRateUtils.HeartRateStatus.BRADYCARDIA -> {
                stateValue = "Bradycardia Detected!"
                stateColor = ContextCompat.getColor(this, android.R.color.holo_red_dark)
            }
            HeartRateUtils.HeartRateStatus.TACHYCARDIA -> {
                stateValue = "Tachycardia Detected!"
                stateColor = ContextCompat.getColor(this, android.R.color.holo_red_dark)
            }
            HeartRateUtils.HeartRateStatus.NORMAL -> {
                stateValue = "Normal"
                stateColor = ContextCompat.getColor(this, android.R.color.holo_green_dark)
            }
        }

        val spannableString = SpannableString("$stateText $stateValue")
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, android.R.color.black)), 0, stateText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(stateColor), stateText.length, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        heartRateState.text = spannableString
    }

    private val heartRateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val heartRate = intent?.getIntExtra("heartRate", 0) ?: 0
            val heartRateStatus = intent?.getSerializableExtra("heartRateStatus") as HeartRateUtils.HeartRateStatus

            heartRateText.text = "$heartRate BPM"
            setHeartRateState(heartRateStatus)
        }
    }
    private val initiationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "InitiationFlagReceived") {
                // Update the status
                nodeStatusText.text = getString(R.string.connected)
                nodeStatusText.setTextColor(
                    ContextCompat.getColor(
                        context,
                        android.R.color.holo_green_dark
                    ))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(heartRateReceiver, IntentFilter("HeartRateUpdate"))
        registerReceiver(initiationReceiver,IntentFilter("InitiationFlagReceived"))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(heartRateReceiver)
        unregisterReceiver(initiationReceiver)
    }
}