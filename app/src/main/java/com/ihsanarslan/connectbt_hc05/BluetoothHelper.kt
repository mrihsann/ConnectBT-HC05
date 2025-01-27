package com.ihsanarslan.connectbt_hc05

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID


class BluetoothManager(private val bluetoothAdapter: BluetoothAdapter?) {
    private var bluetoothSocket: BluetoothSocket? = null
    private val TAG = "LOGGGGG"

    companion object {
        private const val HC05_NAME = "HC-05"
        private val HC05_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    sealed class BluetoothState {
        object Connected : BluetoothState()
        object Disconnected : BluetoothState()
        data class Error(val message: String) : BluetoothState()
    }

    suspend fun connectToDevice(): BluetoothState = withContext(Dispatchers.IO) {
        try {
            if (bluetoothAdapter?.isEnabled != true) {
                return@withContext BluetoothState.Error("Bluetooth is disabled")
            }

            val device = bluetoothAdapter.bondedDevices.find { it.name == HC05_NAME }
                ?: return@withContext BluetoothState.Error("HC-05 not paired")

            bluetoothSocket = device.createRfcommSocketToServiceRecord(HC05_UUID)
            bluetoothSocket?.connect()
            BluetoothState.Connected
        } catch (e: IOException) {
            Log.e(TAG, "Connection failed: ${e.message}")
            disconnect()
            BluetoothState.Error("Connection failed: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
        } catch (e: IOException) {
            Log.e(TAG, "Disconnect failed: ${e.message}")
        }
    }

    suspend fun sendData(data: String): Boolean = withContext(Dispatchers.IO) {
        try {
            bluetoothSocket?.outputStream?.write(data.toByteArray())
            true
        } catch (e: IOException) {
            Log.e(TAG, "Send failed: ${e.message}")
            false
        }
    }

    fun receiveData(): Flow<String> = flow {
        println("0")
        try {
            println("1")
            val buffer = ByteArray(1024)
            println("2")
            while (true) {
                println("3")
                val bytes = bluetoothSocket?.inputStream?.read(buffer)
                println("4")
                if (bytes != null) {
                    println("5")
                    if (bytes > 0) {
                        val received = String(buffer, 0, bytes)
                        println("Data received: $received")
                        Log.d(TAG, "Data received: $received")
                        emit(received)
                    } else {
                        println("No data received")
                        Log.d(TAG, "No data received")
                    }
                } else {
                    println("Bluetooth socket is null")
                    Log.d(TAG, "Bluetooth socket is null")
                    break  // Döngüyü sonlandırmak için break ifadesi
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Receive failed: ${e.message}")
            emit("Error: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)



    fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }
}