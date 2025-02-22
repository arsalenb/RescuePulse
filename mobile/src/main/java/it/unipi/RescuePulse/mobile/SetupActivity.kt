package it.unipi.RescuePulse.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import it.unipi.RescuePulse.mobile.model.SharedViewModel
import it.unipi.RescuePulse.mobile.setupFragments.SetupPagerAdapter

class SetupActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: SetupPagerAdapter
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        sharedViewModel.initSharedPreferences(this.applicationContext)


        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)

        adapter = SetupPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Personal Information"
                1 -> "Emergency Contacts"
                else -> null
            }
        }.attach()

        val buttonFinish: Button = findViewById(R.id.button_finish)
        buttonFinish.setOnClickListener {
            if (sharedViewModel.isFormComplete()) {
                // Save data before navigating to post step
                sharedViewModel.saveContacts()
                sharedViewModel.saveEmergencyServiceNumber()
                sharedViewModel.savePersonalInformation()

                // Navigate to PostSetupActivity
                val intent = Intent(this, PostSetupActivity::class.java)
                startActivity(intent)
            } else {
                // Show a message to the user indicating that not all fields are filled
                Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
