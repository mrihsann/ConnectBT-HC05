package com.ihsanarslan.connectbt_hc05

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class BluetoothHelper(private val bluetoothAdapter: BluetoothAdapter?) {

    companion object {
        private val HC_05_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val TAG = "BluetoothHelper"
    }

    // Bluetooth bağlı cihazları arar
    fun searchPairedDevices(): String {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            return "Bluetooth is disabled or not supported"
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        return if (!pairedDevices.isNullOrEmpty()) {
            pairedDevices.joinToString(separator = "\n") { "${it.name} || ${it.address}" }
        } else {
            "No paired devices found"
        }
    }

    // HC-05 ile bağlantı kurar ve veri okur
    suspend fun connectToHC05(): String? {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            return "Bluetooth is disabled or not supported"
        }

        val hc05Device = bluetoothAdapter.bondedDevices.find { it.name == "HC-05" }
            ?: return "HC-05 not paired"

        return withContext(Dispatchers.IO) {
            var socket: BluetoothSocket? = null
            try {
                socket = hc05Device.createRfcommSocketToServiceRecord(HC_05_UUID)
                socket.connect()

                val inputStream = socket.inputStream
                val buffer = ByteArray(1024)
                val bytes = inputStream.read(buffer)
                val message = String(buffer, 0, bytes)

                socket.close()
                message
            } catch (e: IOException) {
                Log.e(TAG, "Error connecting to HC-05: ${e.message}")
                socket?.close()
                null
            }
        }
    }
}