package com.ihsanarslan.connectbt_hc05

import android.bluetooth.BluetoothAdapter
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomePage() {

    // Context burada LocalContext.current ile alınıyor
    val context = LocalContext.current

    var btDevices by remember { mutableStateOf("") }
    var btReadings by remember { mutableStateOf("") }
    var isConnected by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val bluetoothHelper = BluetoothHelper(bluetoothAdapter)

    val buttonTexts = listOf("Mama", "Su", "Isı")

    Scaffold(){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Linked Bluetooth Devices:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(text = btDevices.ifEmpty { "No devices found" }, fontSize = 16.sp)

            Button(
                onClick = {
                    btDevices = bluetoothHelper.searchPairedDevices()
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search Paired Devices")
            }

            Button(
                onClick = {
                    isLoading = true
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = bluetoothHelper.connectToHC05()
                        withContext(Dispatchers.Main) {
                            btReadings = result ?: "Error: Unable to connect or read data"
                            isConnected = result != null
                            isLoading = false
                        }
                    }
                },
                enabled = btDevices.contains("HC-05") && !isConnected,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Connect to HC-05")
            }

            Button(
                onClick = {
                    btDevices = ""
                    btReadings = ""
                    isConnected = false
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Data")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Readings:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(text = btReadings, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(50.dp))

            // Her bir buton için tıklanabilir bir metin
            buttonTexts.forEach { buttonText ->
                Button(
                    onClick = {
                        // Burada her butona tıklama ile ilgili işlem yapılacak
                        CoroutineScope(Dispatchers.IO).launch {
                            // Burada her butona tıklama ile ilgili işlem yapılacak
                            bluetoothHelper.sendDataToHC05(buttonText) { message ->
                                Toast.makeText(
                                    context,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    enabled = !isLoading && btDevices.contains("HC-05"),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}