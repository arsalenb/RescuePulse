package it.unipi.RescuePulse.wear.utils

import android.content.Context
import android.content.SharedPreferences

object HeartRateUtils {

    private const val PREF_NAME = "HeartRatePrefs"

    // Enum to represent heart rate status
    enum class HeartRateStatus {
        NORMAL, BRADYCARDIA, TACHYCARDIA
    }

    // Function to store heart rate data in SharedPreferences
    fun storeHeartRateData(context: Context, heartRate: Int) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        // Store current timestamp and heart rate
        val timestamp = System.currentTimeMillis()
        editor.putInt(timestamp.toString(), heartRate)
        editor.apply()
    }

    // Function to retrieve heart rate data from SharedPreferences
    fun getHeartRateData(context: Context, startTime: Long, endTime: Long): List<Pair<Long, Int>> {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val allEntries: Map<String, *> = sharedPreferences.all
        val heartRateData = mutableListOf<Pair<Long, Int>>()

        for ((key, value) in allEntries) {
            val timestamp = key.toLongOrNull()
            val heartRate = value as? Int

            if (timestamp != null && heartRate != null && timestamp in startTime..endTime) {
                heartRateData.add(Pair(timestamp, heartRate))
            }
        }

        return heartRateData
    }

    // Function to detect sustained heart rate conditions
    fun detectSustainedHeartRate(
        context: Context,
        periodDurationMillis: Long,
        lowThreshold: Int,
        highThreshold: Int,
        age: Int? = null
    ): HeartRateStatus {
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - periodDurationMillis

        val heartRateData = getHeartRateData(context, startTime, currentTime)

        // If there's no data, assume normal
        if (heartRateData.isEmpty()) {
            return HeartRateStatus.NORMAL
        }

        // Calculate MHR if age is provided (Gellish formula)
        val adjustedHighThreshold = age?.let { (207 - (0.7 * it)).toInt() } ?: highThreshold

        // Determine the low heart rate threshold based on age, using a default value if age is null
        val ageAdjustedLowThreshold = when {
            age == null -> lowThreshold
            age < 30 -> 40
            age in 30..50 -> 50
            else -> 60
        }

        var allLow = true
        var allHigh = true

        for ((_, heartRate) in heartRateData) {
            if (heartRate >= ageAdjustedLowThreshold) {
                allLow = false
            }
            if (heartRate <= 70) {
                allHigh = false
            }
        }

        return when {
            allLow -> HeartRateStatus.BRADYCARDIA
            allHigh -> HeartRateStatus.TACHYCARDIA
            else -> HeartRateStatus.NORMAL
        }
    }

}
