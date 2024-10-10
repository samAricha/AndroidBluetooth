package com.elementalist.enose.data

import android.bluetooth.BluetoothDevice

interface BluetoothListener {
    fun onDeviceFound(device: BluetoothDevice)
    fun onDiscoveryFinished()
    fun onBluetoothDisabled()
}
