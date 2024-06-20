package it.unipi.RescuePulse.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity



class MobileMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonInitiate: Button = findViewById(R.id.button_initiate)
        buttonInitiate.setOnClickListener { v: View? ->
            val intent: Intent =
                Intent(
                    this@MobileMainActivity,
                    SetupActivity::class.java
                )
            startActivity(intent)
        }
    }
}

