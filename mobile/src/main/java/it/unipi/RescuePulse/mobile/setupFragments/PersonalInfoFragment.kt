package it.unipi.RescuePulse.mobile.setupFragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import it.unipi.RescuePulse.mobile.R
import it.unipi.RescuePulse.mobile.model.SharedViewModel
import java.text.SimpleDateFormat
import java.util.*

class PersonalInfoFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
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

        // Observe the LiveData objects from the SharedViewModel
        sharedViewModel.name.observe(viewLifecycleOwner) { name ->
            if (nameEditText.text.toString() != name) {
                nameEditText.setText(name)
            }
        }

        sharedViewModel.surname.observe(viewLifecycleOwner) { surname ->
            if (surnameEditText.text.toString() != surname) {
                surnameEditText.setText(surname)
            }
        }

        sharedViewModel.dob.observe(viewLifecycleOwner) { dob ->
            if (dobEditText.text.toString() != dob) {
                dobEditText.setText(dob)
            }
        }

        sharedViewModel.weight.observe(viewLifecycleOwner) { weight ->
            val weightStr = if (weight != 0) weight.toString() else ""
            if (weightEditText.text.toString() != weightStr) {
                val cursorPosition = weightEditText.selectionStart
                weightEditText.setText(weightStr)
                weightEditText.setSelection(cursorPosition.coerceAtMost(weightStr.length))
            }
        }

        // Add TextWatchers to EditText fields to update SharedViewModel on text change
        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setName(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        surnameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setSurname(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        dobEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setDob(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        weightEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val weightStr = s.toString()
                val weight = if (weightStr.isNotEmpty()) weightStr.toIntOrNull() ?: 0 else 0
                sharedViewModel.setWeight(weight)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        inputDob.setText(sdf.format(calendar.time))
    }
}
