package it.unipi.RescuePulse.mobile.service

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import android.util.Log

class EmergencyCallService : ConnectionService() {

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val connection = EmergencyConnection()
        connection.setInitialized()
        connection.setActive()
        return connection
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val connection = EmergencyConnection()
        connection.setInitialized()
        connection.setActive()
        return connection
    }
}

class EmergencyConnection : Connection() {

    override fun onShowIncomingCallUi() {
        super.onShowIncomingCallUi()
        Log.d("EmergencyConnection", "Incoming call UI shown")
    }

    override fun onAnswer() {
        super.onAnswer()
        setActive()
        Log.d("EmergencyConnection", "Call answered")
    }

    override fun onDisconnect() {
        super.onDisconnect()
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        destroy()
        Log.d("EmergencyConnection", "Call disconnected")
    }
}
