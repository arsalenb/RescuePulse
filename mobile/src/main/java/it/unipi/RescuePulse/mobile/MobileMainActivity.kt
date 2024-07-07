package it.unipi.RescuePulse.mobile

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MobileMainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val permissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.SEND_SMS,
        Manifest.permission.MANAGE_OWN_CALLS
    )

    private val requestMultiplePermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            // All permissions granted, navigate to next activity
            navigateToNextActivity()
        } else {
            // Permission denied, show appropriate message
            permissions.entries.find { !it.value }?.key?.let { handleDeniedPermission(it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val initiateButton: Button = findViewById(R.id.initiate_button)
        initiateButton.setOnClickListener {
            // Request multiple permissions
            requestMultiplePermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestMultiplePermissions() {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(permissionsToRequest)
        } else {
            // All permissions already granted, navigate to next activity
            navigateToNextActivity()
        }
    }

    private fun handleDeniedPermission(permission: String) {
        when (permission) {
            Manifest.permission.READ_CONTACTS -> {
                Toast.makeText(this, "Access to contacts denied", Toast.LENGTH_SHORT).show()
            }
            Manifest.permission.CALL_PHONE -> {
                Toast.makeText(this, "Phone call permission denied", Toast.LENGTH_SHORT).show()
            }
            Manifest.permission.SEND_SMS -> {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
            }
            Manifest.permission.MANAGE_OWN_CALLS -> {
                Toast.makeText(this, "Manage own calls permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToNextActivity() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        if (sharedPreferences.contains("name")) {
            // Data exists, go to PostSetupActivity
            val intent = Intent(this, PostSetupActivity::class.java)
            startActivity(intent)
        } else {
            // No data, go to SetupActivity
            val intent = Intent(this, SetupActivity::class.java)
            startActivity(intent)
        }
    }
}
