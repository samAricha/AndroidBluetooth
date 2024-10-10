package com.elementalist.enose.data

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity

object ServiceLocator {
    private var bluetoothService: BluetoothServiceNew? = null

    // Method to initialize the BluetoothService
    fun initBluetoothService(context: Context) {
        if (bluetoothService == null) {
            val serviceIntent = Intent(context, BluetoothServiceNew::class.java)
            context.startService(serviceIntent)
            // Bind to the service if needed
            // (You can implement a method to bind to the service and store the binder)
        }
    }

    // Add this to ServiceLocator to bind to the service
    fun bindBluetoothService(activity: Activity) {
        activity.bindService(
            Intent(activity, BluetoothServiceNew::class.java),
            object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    bluetoothService = (service as BluetoothServiceNew.BluetoothBinder).service
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    bluetoothService = null
                }
            },
            Context.BIND_AUTO_CREATE
        )
    }


    // Method to get the BluetoothService
    fun getBluetoothService(): BluetoothServiceNew {
        return bluetoothService ?: throw IllegalStateException("BluetoothService is not initialized.")
    }

    // Optional: Method to clean up the service when no longer needed
    fun cleanUp() {
        bluetoothService = null
    }
}
