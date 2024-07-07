package it.unipi.RescuePulse.mobile.utils

import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {

    fun calculateAgeFromDob(dobString: String?): Int? {
        if (dobString.isNullOrEmpty()) {
            return null
        }

        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dob = formatter.parse(dobString)
            if (dob != null) {
                val dobCalendar = Calendar.getInstance().apply { time = dob }
                val today = Calendar.getInstance()
                var age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
                if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }
                age
            } else {
                null
            }
        } catch (e: ParseException) {
            Log.e("DateUtils", "Error parsing DOB: ${e.message}")
            null
        }
    }
}
