package org.winplus.serial

import android.util.Log
import org.winplus.serial.utils.SerialPortFinder
import org.winplus.serial.utils.Utils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalArgumentException

/**
 *  Created by Jess on 2019/12/27
 */
class SerialPort() {

    private val TAG = SerialPort::class.java.simpleName

    private val serialPortFinder = SerialPortFinder()
    private var serialPort: org.winplus.serial.utils.SerialPort? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var readThread : ReadThread ?= null

    private var isOpen = false

    fun getAllDevicePath(): Array<String> {
        return serialPortFinder.allDevicesPath
    }

    fun open(path: String, baudRate: Int): org.winplus.serial.utils.SerialPort? {
        if (path.isBlank() || baudrate == 0) {
            throw IllegalArgumentException()
        }

        if (isOpen) {
            return serialPort
        }

        serialPort = org.winplus.serial.utils.SerialPort(File(path), baudRate, 0)

        if (serialPort != null) {
            try {
                outputStream = serialPort?.outputStream
                inputStream = serialPort?.inputStream
                readThread = ReadThread()
                readThread.start()
                isOpen = true
            } catch (e: IOException) {
                Log.d(TAG, "cannot open stream")
            }
        }

        return serialPort
    }

    fun close() {
        serialPort?.close()
        serialPort = null
        isOpen = false
    }

    fun getSerialPort(): org.winplus.serial.utils.SerialPort? {
        return serialPort
    }

    fun send(data: ByteArray) {
        try {
            outputStream?.write(data)
        } catch (e: IOException) {
            Log.d(TAG, "cannot write data")
        }
    }

    fun sendTxt(txt: String) {
        val byteArray = txt.toByteArray()
        send(byteArray)
    }

    fun sendHex(hex: String) {
        val byteArray = Utils.hexStringToBytes(hex)
        send(byteArray)
    }

    private inner class ReadThread : Thread() {
        override fun run() {
            super.run()
            while (!isInterrupted) {
                if (inputStream == null) return
                val buffer = ByteArray(512)
                val size = inputStream?.read(buffer) ?: 0
                if (size > 0) {
                    Log.d(TAG, "-----------$size")
                    val b = Arrays.copyOfRange(buffer, 0, size)
                    var sta = ""
                    for (i in 0 until size) {
                        sta += b[i].toChar()
                    }
                    Log.d(TAG, sta)
//                    val data = ComData(port, buffer, size)
//                    onDataReceived(data)
                }
            }
        }
    }


}