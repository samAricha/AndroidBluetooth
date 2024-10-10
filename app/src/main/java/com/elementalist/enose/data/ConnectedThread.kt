package com.elementalist.enose.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.regex.Pattern

class ConnectedThread(
    private val connectedBluetoothSocket: BluetoothSocket,
    private val scaleType: Int
) : Thread() {
    private val connectedInputStream: InputStream = connectedBluetoothSocket.inputStream
    private val connectedOutputStream: OutputStream = connectedBluetoothSocket.outputStream
    var platform = scaleType == 0
    var bridgeScale = scaleType != 0

    var group_number = 1
    private val dataLiveData = MutableLiveData<Double>()

    fun getDataLiveData(): LiveData<Double> {
        return dataLiveData
    }

    private val regex = Pattern.compile("\\d+\\.\\d+")
    private val bridgeRegex = Pattern.compile("(\\+\\w{9}\\b)")

    override fun run() {
        val buffer = ByteArray(1024)
        var bytes: Int
        val scope = CoroutineScope(Dispatchers.IO)

        while (true) {
            try {
                if (platform || bridgeScale) sleep(500)

                bytes = connectedInputStream.read(buffer)
                var strReceived = String(buffer, 0, bytes)
                Timber.tag("RECEIVED").e(strReceived)

                if (platform) {
                    strReceived = handlePlatformData(strReceived)
                }

                if (bridgeScale) {
                    strReceived = handleBridgeData(strReceived)
                }

                if (strReceived.isNotEmpty() && isValidNumericInput(strReceived)) {
                    val matcher = regex.matcher(strReceived)
                    if (matcher.find()) {
                        val value = matcher.group(group_number).toDouble()
                        dataLiveData.postValue(value)
                        Timber.tag("ACTUAL_SCALE_VALUE").d(value.toString())
                        EventBus.getDefault().post(ScaleDataStreamEvent(value))
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                cancel()
                break
            }
        }
    }

    private fun handlePlatformData(strReceived: String): String {
        if (strReceived.contains("ST") && strReceived.contains("kg")) {
            val list = strReceived.split("kg", ignoreCase = true).filter { it.contains("ST") }
            return list.firstOrNull() ?: strReceived
        }
        if (strReceived.contains("GS") && strReceived.contains("KG")) {
            val list = strReceived.split("kg", ignoreCase = true).filter { it.contains("GS") }
            return list.firstOrNull() ?: strReceived
        }
        return strReceived
    }

    private fun handleBridgeData(strReceived: String): String {
        val matcher = bridgeRegex.matcher(strReceived)
        return if (matcher.find()) {
            matcher.group(1)?.substring(1, matcher.group(1).length - 3) ?: strReceived
        } else {
            ""
        }
    }

    private fun isValidNumericInput(strReceived: String): Boolean {
        return strReceived.trim().replace("\u0002", "").replace("\u0003", "").isNotEmpty() &&
                (strReceived.contains("\u0002") || strReceived.contains("\u0003") || strReceived.isNumeric())
    }

    private fun String.isNumeric(): Boolean {
        val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
        return this.matches(regex)
    }

    fun write(buffer: ByteArray) {
        try {
            connectedOutputStream.write(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun cancel() {
        try {
            connectedBluetoothSocket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
