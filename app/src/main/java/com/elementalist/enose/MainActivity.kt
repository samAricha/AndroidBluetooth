package com.elementalist.enose

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.elementalist.enose.data.BluetoothService
import com.elementalist.enose.data.BluetoothServiceNew
import com.elementalist.enose.data.BluetoothViewModel
import com.elementalist.enose.data.ConnectThread
import com.elementalist.enose.data.ServiceLocator
import com.elementalist.enose.ui.NavigationGraph
import com.elementalist.enose.ui.screens.MainViewModel
import com.elementalist.enose.ui.theme.ENoseTheme
import com.elementalist.enose.util.MY_TAG
import com.elementalist.enose.util.enableLocation
import com.elementalist.enose.util.isLocationEnabled


class MainActivity : ComponentActivity() {

    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var viewModel: MainViewModel

    private lateinit var bluetoothViewModel: BluetoothViewModel
    private var bluetoothService: BluetoothServiceNew? = null
    private var serviceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter


        bluetoothViewModel = ViewModelProvider(this).get(BluetoothViewModel::class.java)

        // Initialize the ViewModel
        bluetoothViewModel = ViewModelProvider(this).get(BluetoothViewModel::class.java)

        // Initialize and bind to the BluetoothService via ServiceLocator
        ServiceLocator.initBluetoothService(this)
        ServiceLocator.bindBluetoothService(this)
        // Pass ViewModel to BluetoothService
        bluetoothService?.setViewModel(bluetoothViewModel)


        if (!bluetoothAdapter.isEnabled) {
            enableBluetooth()
        }
        //This specific action is required since my personal mobile needs GPS enabled to discover devices
        //(not written in any official documentation but needed nonetheless)
        if (!isLocationEnabled(this) && Build.VERSION.SDK_INT <= 30) {
            Toast.makeText(
                this,
                "Location should be enabled since Location services are needed on some devices for correctly locating other Bluetooth devices",
                Toast.LENGTH_SHORT
            ).show()
            enableLocation(this)
        }

        viewModel = MainViewModel(bluetoothAdapter)

        // Register for broadcasts when a device is discovered
        val filter = IntentFilter()
        //register a broadcast receiver to check if the user disables his Bluetooth (or it has it already disabled)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        //receivers for device discovering
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)


        setContent {
            ENoseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    NavigationGraph(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()

        // Make sure we're not doing discovery anymore
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver)

        //close open socket
        viewModel.selectedDevice?.let { ConnectThread(device = it,viewModel = viewModel).cancel() }
    }

    private val mReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                //when discovery finds a device
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    Log.i(MY_TAG, "device found")
                    if (device != null &&
                        device.name != null
                    ) {
                        viewModel.addDiscoveredDevice(device)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i(MY_TAG, "ACTION_DISCOVERY_FINISHED")
                    viewModel.scanningFinished()
                    //if there are no device show proper message
                    if (viewModel.discoveredDevices.isEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "Unfortunately no devices were found in your vicinity",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(applicationContext, "Scan finished", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    //Since our app needs bluetooth to work correctly we don't let the user turn it off
                    if (bluetoothAdapter.state == BluetoothAdapter.STATE_OFF
                    ) {
                        enableBluetooth()
                    }
                }
            }
        }
    }

    private val enableBluetoothResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Bluetooth Enabled!", Toast.LENGTH_SHORT).show()
        } else {

            Toast.makeText(this, "Bluetooth is required for this app to run", Toast.LENGTH_SHORT)
                .show()
            this.finish()
        }
    }

    /**
     * Pop-up activation for enabling Bluetooth
     *
     */
    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothResultLauncher.launch(enableBtIntent)
    }





    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BluetoothServiceNew.BluetoothBinder
            bluetoothService = binder.service
            bluetoothService?.setViewModel(bluetoothViewModel)
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bluetoothService = null
            serviceBound = false
        }
    }


}

