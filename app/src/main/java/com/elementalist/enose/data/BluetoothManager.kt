package com.elementalist.enose.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.elementalist.enose.util.MY_TAG
import timber.log.Timber

class BluetoothManager(
    private val context: Context,
    private val listener: BluetoothListener
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothReceiver: BluetoothReceiver? = null

    init {
        // Register the BluetoothReceiver during initialization
        registerReceiver()
    }

    private fun registerReceiver() {
        // Utilize the BluetoothReceiver to handle events and register it
        bluetoothReceiver = BluetoothReceiver.register(context, listener)
    }

    @SuppressLint("MissingPermission")
    fun discoverDevices() {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter?.startDiscovery()
        Timber.tag(MY_TAG).i("discovery started")
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery(listener: BluetoothListener) {
        bluetoothAdapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
        bluetoothReceiver?.let {
            BluetoothReceiver.unregister(context, it)
            bluetoothReceiver = null
        }
    }


    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun enableBluetooth(activityResultLauncher: ActivityResultLauncher<Intent>) {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activityResultLauncher.launch(enableBtIntent)
        }
    }

    @SuppressLint("MissingPermission")
    fun disableBluetooth() {
        // Disable Bluetooth if it is enabled
        bluetoothAdapter?.disable()
    }

    fun unregisterReceiver() {
        // Unregister the receiver when it's no longer needed
        bluetoothReceiver?.let {
            BluetoothReceiver.unregister(context, it)
            bluetoothReceiver = null
        }
    }


}
