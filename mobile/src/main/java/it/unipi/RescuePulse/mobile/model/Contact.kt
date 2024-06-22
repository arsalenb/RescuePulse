package it.unipi.RescuePulse.mobile.model

data class Contact(val displayName: String, val phoneNumber: String?) {
    // Companion object for handling serialization and deserialization
    companion object {
        fun fromString(string: String): Contact {
            val parts = string.split(",")
            val displayName = parts[0]
            val phoneNumber = if (parts.size > 1) parts[1] else null
            return Contact(displayName, phoneNumber)
        }
    }

    // Override toString() to convert Contact to string representation
    override fun toString(): String {
        return "$displayName, $phoneNumber"
    }
}
