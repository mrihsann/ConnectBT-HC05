package com.ihsanarslan.connectbt_hc05

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {

    companion object {
        private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBluetoothPermissions()

        setContent{
            HomePage()
        }

    }


    // Bluetooth izinlerini isteme
    private fun requestBluetoothPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            BLUETOOTH_PERMISSION_REQUEST_CODE
        )
    }
}