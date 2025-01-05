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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomePage(bluetoothManager: BluetoothManager) {
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    val context = LocalContext.current
    var connectionState by remember {
        mutableStateOf<BluetoothManager.BluetoothState>(BluetoothManager.BluetoothState.Disconnected)
    }
    var receivedDataList by remember { mutableStateOf<List<ReceivedData>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val actions = listOf(
        Action("Mama", Icons.Default.Restaurant),
        Action("Su", Icons.Default.WaterDrop),
        Action("Isı", Icons.Default.Thermostat)
    )
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = connectionState is BluetoothManager.BluetoothState.Connected,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                ControlPanel(
                    actions = actions,
                    isEnabled = connectionState is BluetoothManager.BluetoothState.Connected,
                    onActionClick = { action ->
                        scope.launch {
                            val success = bluetoothManager.sendData(action.name)
                            val message = if (success) "${action.name} gönderildi" else "Gönderim başarısız"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Status Section
            ConnectionStatusSection(
                connectionState = connectionState,
                selectedDevice = selectedDevice,
                onDisconnect = {
                    scope.launch {
                        bluetoothManager.disconnect()
                        connectionState = BluetoothManager.BluetoothState.Disconnected
                        selectedDevice = null
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Device Selection Section (when disconnected)
            AnimatedVisibility(
                visible = connectionState is BluetoothManager.BluetoothState.Disconnected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                DeviceSelectionSection(
                    pairedDevices = pairedDevices,
                    onRefresh = { pairedDevices = bluetoothManager.getPairedDevices() },
                    onDeviceSelect = { device ->
                        scope.launch {
                            selectedDevice = device
                            connectionState = bluetoothManager.connectToDevice()
                            if (connectionState is BluetoothManager.BluetoothState.Connected) {
                                bluetoothManager.receiveData().collectLatest { data ->
                                    val timestamp = System.currentTimeMillis()
                                    receivedDataList = receivedDataList + ReceivedData(data, timestamp)
                                }
                            }
                        }
                    }
                )
            }

            // Received Data Section
            AnimatedVisibility(
                visible = connectionState is BluetoothManager.BluetoothState.Connected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ReceivedDataSection(
                    receivedDataList = receivedDataList,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}

data class Action(
    val name: String,
    val icon: ImageVector
)

data class ReceivedData(
    val content: String,
    val timestamp: Long
)

@Composable
fun ConnectionStatusSection(
    connectionState: BluetoothManager.BluetoothState,
    selectedDevice: BluetoothDevice?,
    onDisconnect: () -> Unit
) {
    when (connectionState) {
        is BluetoothManager.BluetoothState.Connected -> {
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BluetoothConnected,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = selectedDevice?.name ?: "Bağlı Cihaz",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = selectedDevice?.address ?: "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    IconButton(
                        onClick = onDisconnect,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.BluetoothDisabled,
                            contentDescription = "Bağlantıyı Kes",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        is BluetoothManager.BluetoothState.Error -> {
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = connectionState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        else -> { /* Disconnected state - no UI */ }
    }
}

@Composable
fun DeviceSelectionSection(
    pairedDevices: List<BluetoothDevice>,
    onRefresh: () -> Unit,
    onDeviceSelect: (BluetoothDevice) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                FilledTonalIconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Yenile")
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
                        onClick = { onDeviceSelect(device) }
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
                                FilledTonalIconButton(
                                    onClick = { onDeviceSelect(device) }
                                ) {
                                    Icon(
                                        Icons.Default.AddLink,
                                        contentDescription = "Bağlan"
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReceivedDataSection(
    receivedDataList: List<ReceivedData>,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alınan Veriler",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${receivedDataList.size} veri",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(receivedDataList.size) { index ->
                    val data = receivedDataList[receivedDataList.size - 1 - index]
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = data.content,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = dateFormat.format(Date(data.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlPanel(
    actions: List<Action>,
    isEnabled: Boolean,
    onActionClick: (Action) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Kontroller",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                actions.forEach { action ->
                    ElevatedButton(
                        onClick = { onActionClick(action) },
                        enabled = isEnabled,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(action.icon, contentDescription = null)
                            Text(action.name)
                        }
                    }
                }
            }
        }
    }
}