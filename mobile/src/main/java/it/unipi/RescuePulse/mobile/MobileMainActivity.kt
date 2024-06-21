package it.unipi.RescuePulse.mobile

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import android.widget.Button

class MobileMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val initiateButton: Button = findViewById(R.id.initiate_button)
        initiateButton.setOnClickListener {
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
}
