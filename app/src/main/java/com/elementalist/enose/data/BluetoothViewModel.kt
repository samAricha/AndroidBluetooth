package com.elementalist.enose.data

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BluetoothViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothService: BluetoothServiceNew by lazy {
        ServiceLocator.getBluetoothService()
    }

    private val _connectionStatus = MutableLiveData<Boolean>()
    val connectionStatus: LiveData<Boolean> get() = _connectionStatus

    private val _receivedData = MutableLiveData<Any>()
    val receivedData: LiveData<Any> get() = _receivedData

    // LiveData for observing Bluetooth state changes
    private val _foundDevices = MutableLiveData<List<BluetoothDevice>>()
    val foundDevices: LiveData<List<BluetoothDevice>> get() = _foundDevices


    private val _discoveryFinished = MutableLiveData<Unit>()
    val discoveryFinished: LiveData<Unit> get() = _discoveryFinished

    private val _bluetoothDisabled = MutableLiveData<Unit>()
    val bluetoothDisabled: LiveData<Unit> get() = _bluetoothDisabled



    fun startDeviceDiscovery() {
        bluetoothService.startDiscoveringDevices()
    }

    fun onDeviceFound(device: BluetoothDevice) {
        val currentDevices = _foundDevices.value?.toMutableList() ?: mutableListOf()
        currentDevices.add(device)
        _foundDevices.value = currentDevices // Update LiveData with new device
    }

    fun connectToDevice(deviceAddress: String, scaleType: Int) {
        bluetoothService.connectToDevice(deviceAddress, scaleType)
    }

    fun sendData(data: ByteArray) {
        bluetoothService.sendData(data)
    }

    fun onDiscoveryFinished() {
        // Handle discovery finished
    }

    fun onBluetoothDisabled() {
        // Handle Bluetooth disabled
    }

}
