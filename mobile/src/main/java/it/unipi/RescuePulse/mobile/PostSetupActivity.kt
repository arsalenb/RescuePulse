package it.unipi.RescuePulse.mobile

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class PostSetupActivity : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var surnameTextView: TextView
    private lateinit var dobTextView: TextView
    private lateinit var weightTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_setup)

        nameTextView = findViewById(R.id.text_name)
        surnameTextView = findViewById(R.id.text_surname)
        dobTextView = findViewById(R.id.text_dob)
        weightTextView = findViewById(R.id.text_weight)

        val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        nameTextView.text = sharedPreferences.getString("name", "")
        surnameTextView.text = sharedPreferences.getString("surname", "")
        dobTextView.text = sharedPreferences.getString("dob", "")
        weightTextView.text = sharedPreferences.getInt("weight", 0).toString()
    }
}
