package com.ihsanarslan.connectbt_hc05

import android.bluetooth.BluetoothDevice
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

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

    var showDisconnectedDialog by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }

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
            // Status Banner
            AnimatedVisibility(
                visible = connectionState is BluetoothManager.BluetoothState.Error,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = (connectionState as? BluetoothManager.BluetoothState.Error)?.message
                                    ?: "Bağlantı hatası",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        FilledTonalButton(onClick = { showDisconnectedDialog = false }) {
                            Text("Tamam")
                        }
                    }
                }
            }

            // Connected Device Status
            AnimatedVisibility(
                visible = connectionState is BluetoothManager.BluetoothState.Connected,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = selectedDevice?.name ?: "Bağlı Cihaz",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = selectedDevice?.address ?: "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    bluetoothManager.disconnect()
                                    connectionState = BluetoothManager.BluetoothState.Disconnected
                                    selectedDevice = null
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.BluetoothDisabled,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bağlantıyı Kes")
                        }
                    }
                }
            }

            // Paired Devices Section
            AnimatedVisibility(
                visible = connectionState is BluetoothManager.BluetoothState.Disconnected,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
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
                                Icon(Icons.Default.Refresh, contentDescription = null)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .heightIn(max = 200.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pairedDevices.size) { index ->
                                val device = pairedDevices[index]
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        scope.launch {
                                            selectedDevice = device
                                            connectionState = bluetoothManager.connectToDevice()
                                            if (connectionState is BluetoothManager.BluetoothState.Connected) {
                                                bluetoothManager.receiveData().collectLatest { data ->
                                                    receivedDataList = receivedDataList + data
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    ListItem(
                                        headlineContent = {
                                            Text(device.name ?: "Bilinmeyen Cihaz")
                                        },
                                        supportingContent = {
                                            Text(device.address)
                                        },
                                        leadingContent = {
                                            Icon(
                                                Icons.Default.Bluetooth,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        trailingContent = {
                                            Icon(
                                                Icons.Default.ChevronRight,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Received Data Section
            AnimatedVisibility(
                visible = connectionState is BluetoothManager.BluetoothState.Connected,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .height(250.dp)  // Sabit yükseklik
                        .fillMaxWidth()
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
                            modifier = Modifier
                                .weight(1f)  // Kalan alanı doldur
                                .fillMaxWidth(),
                            reverseLayout = true,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(receivedDataList.size) { index ->
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DataUsage,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = receivedDataList[receivedDataList.size - 1 - index],
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            AnimatedVisibility(
                visible = connectionState is BluetoothManager.BluetoothState.Connected,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
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
                                ElevatedButton(
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

    // Disconnection Dialog
    if (showDisconnectedDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectedDialog = false },
            title = { Text("Bağlantı Kesildi") },
            text = { Text("Bluetooth bağlantısı kesildi. Yeniden bağlanmak ister misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            connectionState = bluetoothManager.connectToDevice()
                            showDisconnectedDialog = false
                        }
                    }
                ) {
                    Text("Yeniden Bağlan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectedDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}