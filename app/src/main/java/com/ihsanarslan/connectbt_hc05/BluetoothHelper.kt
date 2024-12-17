package com.ihsanarslan.connectbt_hc05

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*


// Bluetooth işlemlerini yönetmek için yardımcı sınıf
class BluetoothHelper(private val bluetoothAdapter: BluetoothAdapter?) {

    companion object {
        private val HC_05_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

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

                socket.close()
                "Successfully connected to HC-05"
            } catch (e: IOException) {
                Log.e("Bluetooth", "Error connecting to HC-05: ${e.message}")
                socket?.close()
                null
            }
        }
    }

    fun sendDataToHC05(data: String, onResult: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = connectToHC05() // Bağlantıyı kuruyor
            if (result != null) {
                val sendResult = withContext(Dispatchers.IO) {
                    try {
                        val hc05Device = bluetoothAdapter?.bondedDevices?.find { it.name == "HC-05" }
                        val socket = hc05Device?.createRfcommSocketToServiceRecord(HC_05_UUID)
                        socket?.connect()
                        socket?.outputStream?.write(data.toByteArray())
                        socket?.close()
                        "Data sent successfully: $data"
                    } catch (e: IOException) {
                        Log.e("Bluetooth", "Error sending data to HC-05: ${e.message}")
                        "Failed to send data"
                    }
                }
                onResult(sendResult)
            } else {
                onResult("Failed to connect to HC-05")
            }
        }
    }

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
}