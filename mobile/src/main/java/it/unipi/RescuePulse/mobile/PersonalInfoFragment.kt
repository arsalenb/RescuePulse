package it.unipi.RescuePulse.mobile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class PersonalInfoFragment : Fragment() {
    private lateinit var inputDob: EditText
    private lateinit var calendar: Calendar

    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var dobEditText: EditText
    private lateinit var weightEditText: EditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_personal_info, container, false)

        inputDob = view.findViewById(R.id.input_dob)
        inputDob.setOnClickListener {
            showDatePicker()
        }

        calendar = Calendar.getInstance()

        nameEditText = view.findViewById(R.id.input_name)
        surnameEditText = view.findViewById(R.id.input_surname)
        dobEditText = inputDob
        weightEditText = view.findViewById(R.id.input_weight)

        return view
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            requireContext(), { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set max date to current date and show calendar view
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        inputDob.setText(sdf.format(calendar.time))
    }

    override fun onPause() {
        super.onPause()
        savePersonalInformation()
    }

    private fun savePersonalInformation() {
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("name", nameEditText.text.toString())
        editor.putString("surname", surnameEditText.text.toString())
        editor.putString("dob", dobEditText.text.toString())
        editor.putInt("weight", weightEditText.text.toString().toInt())
        editor.apply()
    }
}
