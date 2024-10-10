package com.elementalist.enose.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.ViewModel
import com.elementalist.enose.R
import com.elementalist.enose.data.ConnectThread
import com.elementalist.enose.util.MY_TAG
import com.elementalist.enose.R.drawable.ok

class MainViewModel(
    private val bluetoothAdapter: BluetoothAdapter
) : ViewModel() {

    var discoveredDevices = mutableStateListOf<BluetoothDevice>()
        private set

    var scanningForDevices = mutableStateOf(false)
        private set

    var selectedDevice by mutableStateOf<BluetoothDevice?>(null)
        private set




    var image by mutableStateOf(R.drawable.flag2)
        private set

    var connectionState by mutableStateOf(StatesOfConnectionEnum.CLIENT_STARTED)
        private set

    var lockedWeight by mutableStateOf<String?>(null)
        private set

    var currentWeight by mutableStateOf<String?>(null)

    // Track whether the weight has been locked
    var isWeightLocked by mutableStateOf(false)
        private set



    fun addDiscoveredDevice(device: BluetoothDevice) {
        if (!discoveredDevices.contains(device)) {
            discoveredDevices.add(device)
        }
    }


    fun scanningFinished() {
        scanningForDevices.value = false
    }

    @SuppressLint("MissingPermission")
    fun scanForDevices() {
        scanningForDevices.value = true
        //savedPairedDevices = bluetoothAdapter.bondedDevices
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()
        Log.i(MY_TAG, "discovery started")
    }

    fun selectDevice(device: BluetoothDevice) {
        selectedDevice = device
    }

    fun lockInWeight() {
        lockedWeight = currentWeight
        isWeightLocked = true
        changeStateOfConnectivity(StatesOfConnectionEnum.WEIGHT_LOCKED)
    }

    fun restartWeighing() {
        lockedWeight = currentWeight
        isWeightLocked = false
        changeStateOfConnectivity(StatesOfConnectionEnum.CLIENT_STARTED)
    }

    /**
     * Connect to the selected device and start a server socket connection
     * and then listen to data from connected socket
     */
    @SuppressLint("MissingPermission")
    fun startBluetoothService(){
        changeStateOfConnectivity(StatesOfConnectionEnum.CLIENT_STARTED)
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery()
        //Listen for data from selected device
        selectedDevice?.let {
            ConnectThread(it, this).start()
        }
        changeStateOfConnectivity(StatesOfConnectionEnum.RECEIVING_RESPONSE)
    }


    @SuppressLint("MissingPermission")
    fun changeStateOfConnectivity(
        newState: StatesOfConnectionEnum,
        dataReceived: String? = null
    ) {
        connectionState = newState
        when (newState) {
            StatesOfConnectionEnum.CLIENT_STARTED -> {
                image = R.drawable.flag2
            }
            StatesOfConnectionEnum.RECEIVING_RESPONSE -> {
                currentWeight = dataReceived
            }
            StatesOfConnectionEnum.WEIGHT_LOCKED -> {
                lockedWeight = currentWeight // Lock the current weight
                image = ok
            }
            StatesOfConnectionEnum.ERROR -> {
                val annotatedText = buildAnnotatedString {
                    append("An error occurred:")
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        if (dataReceived != null) {
                            append(dataReceived)
                        }
                    }
                }
                image = R.drawable.harvest
            }
        }
    }

}

