package com.ihsanarslan.connectbt_hc05

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun HomePage(bluetoothManager: BluetoothManager) {
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    val context = LocalContext.current
    var connectionState by remember {
        mutableStateOf<BluetoothManager.BluetoothState>(BluetoothManager.BluetoothState.Disconnected)
    }
    var receivedDataList by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val actions = listOf("Mama", "Su", "Isı")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bluetooth Durumu",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (connectionState) {
                                is BluetoothManager.BluetoothState.Connected -> "Bağlı"
                                is BluetoothManager.BluetoothState.Disconnected -> "Bağlı Değil"
                                is BluetoothManager.BluetoothState.Error ->
                                    "Hata: ${(connectionState as BluetoothManager.BluetoothState.Error).message}"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    connectionState = bluetoothManager.connectToDevice()
                                    if (connectionState is BluetoothManager.BluetoothState.Connected) {
                                        bluetoothManager.receiveData().collectLatest { data ->
                                            receivedDataList = receivedDataList + data
                                        }
                                    }
                                }
                            },
                            enabled = connectionState is BluetoothManager.BluetoothState.Disconnected
                        ) {
                            Text(if (connectionState is BluetoothManager.BluetoothState.Connected) "Bağlı" else "Bağlan")
                        }
                    }
                }
            }

            // Paired Devices Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Eşleşmiş Cihazlar",
                            style = MaterialTheme.typography.titleMedium
                        )

                        FilledTonalButton(
                            onClick = { pairedDevices = bluetoothManager.getPairedDevices() }
                        ) {
                            Text("Yenile")
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 150.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(pairedDevices.size) { index ->
                            ListItem(
                                headlineContent = {
                                    Text(pairedDevices[index].name ?: "Bilinmeyen Cihaz")
                                },
                                supportingContent = {
                                    Text(pairedDevices[index].address)
                                }
                            )
                        }
                    }
                }
            }

            // Received Data Section
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Alınan Veriler",
                        style = MaterialTheme.typography.titleMedium
                    )

                    LazyColumn(
                        reverseLayout = true,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(receivedDataList.size) { index ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = receivedDataList[receivedDataList.size - 1 - index],
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Kontroller",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        actions.forEach { action ->
                            Button(
                                onClick = {
                                    scope.launch {
                                        val success = bluetoothManager.sendData(action)
                                        Toast.makeText(
                                            context,
                                            if (success) "$action gönderildi" else "Gönderim başarısız",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                enabled = connectionState is BluetoothManager.BluetoothState.Connected,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(action)
                            }
                        }
                    }
                }
            }
        }
    }
}