package com.elementalist.enose.data

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import java.io.IOException
import java.util.UUID

class BluetoothServiceNew : Service(), BluetoothListener {
    
    // Binder for clients
    inner class BluetoothBinder : Binder() {
        val service: BluetoothServiceNew
            get() = this@BluetoothServiceNew
    }

    private val binder: IBinder = BluetoothBinder()

    // Bluetooth related variables
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var mConnectedThread: ConnectedThread? = null
    private var mHandler: Handler? = null
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothViewModel: BluetoothViewModel

    // UUID for Bluetooth
    private val BTMODULEUUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // "random" unique identifier

    fun setViewModel(viewModel: BluetoothViewModel) {
        bluetoothViewModel = viewModel
    }


    override fun onCreate() {
        super.onCreate()
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        bluetoothManager = BluetoothManager(this, object : BluetoothListener {
            override fun onDeviceFound(device: BluetoothDevice) {
                // Handle device found
            }

            override fun onDiscoveryFinished() {
                // Handle discovery finished
            }

            override fun onBluetoothDisabled() {
                // Handle Bluetooth disabled
            }
        })
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val deviceAddress = intent.getStringExtra("device_address")
        val scaleType = intent.getIntExtra("scale_type", 0)
        connectToDevice(deviceAddress, scaleType)

        bluetoothManager.discoverDevices() // Start discovering devices
        return START_STICKY
    }


    fun connectToDevice(deviceAddress: String?, scaleType: Int) {
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        
        // Start the Bluetooth connection in a background thread
        object : Thread() {
            @SuppressLint("MissingPermission")
            override fun run() {
                try {
                    bluetoothSocket = device?.createRfcommSocketToServiceRecord(BTMODULEUUID)
                    bluetoothSocket?.connect()
                    // Start the connected thread
                    mConnectedThread = ConnectedThread(bluetoothSocket!!, scaleType)
                    mConnectedThread?.start()
                    // Notify connection success through LiveData
                    mConnectedThread?.getDataLiveData()?.observeForever { value ->
                        // Handle the received data here
                    }
                } catch (e: IOException) {
                    // Handle connection failure
                    e.printStackTrace()
                }
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    fun startDiscoveringDevices() {
        bluetoothManager.discoverDevices()
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovering() {
        bluetoothAdapter?.cancelDiscovery()
        bluetoothManager.unregisterReceiver()
    }

    fun sendData(data: ByteArray) {
        mConnectedThread?.write(data) // Use the ConnectedThread's write method
    }

    // Implement the BluetoothListener methods
    override fun onDeviceFound(device: BluetoothDevice) {
        bluetoothViewModel.onDeviceFound(device)
    }

    override fun onDiscoveryFinished() {
        bluetoothViewModel.onDiscoveryFinished()
    }

    override fun onBluetoothDisabled() {
        bluetoothViewModel.onBluetoothDisabled()
    }

    // Add other necessary service methods (e.g., cleanup, disconnect)
}
