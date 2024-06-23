package it.unipi.RescuePulse.mobile.model

data class Contact(val displayName: String, val phoneNumber: String?) {
    // Companion object for handling serialization and deserialization
    companion object {
        private fun fromString(string: String): Contact {
            val parts = string.split(",")
            val displayName = parts[0]
            val phoneNumber = if (parts.size > 1) parts[1] else null
            return Contact(displayName, phoneNumber)
        }

        // Deserialize a JSON string into a list of Contact objects
        fun fromJsonList(json: String?): MutableList<Contact> {
            val contactList = mutableListOf<Contact>()
            json?.let {
                val parts = it.split(";")
                for (part in parts) {
                    contactList.add(fromString(part))
                }
            }
            return contactList
        }

        // Serialize a list of Contact objects into a JSON string
        fun toJsonList(contacts: MutableList<Contact>): String {
            return contacts.joinToString(";") { it.toString() }
        }

    }
    override fun toString(): String {
        return "$displayName, $phoneNumber"
    }
}
