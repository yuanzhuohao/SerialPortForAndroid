package org.winplus.serial

import android.util.Log
import org.winplus.serial.utils.SerialPortFinder
import org.winplus.serial.utils.Utils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.*

/**
 *  Created by Jess on 2019/12/27
 */
 class Serialer() {

    private val TAG = Serialer::class.java.simpleName

    private val serialPortFinder = SerialPortFinder()
    private var serialPort: org.winplus.serial.utils.SerialPort? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var readThread : ReadThread ?= null

    private var isOpen = false

    private var onDataReceiveListener: (data: ByteArray) -> Unit = {}

    fun getAllDevicePath(): Array<String> {
        return serialPortFinder.allDevicesPath
    }

    fun isOpen(): Boolean {
        return isOpen
    }

    fun open(path: String, baudRate: Int): org.winplus.serial.utils.SerialPort? {
        if (path.isBlank() || baudRate == 0) {
            throw IllegalArgumentException()
        }

        if (isOpen) {
            return serialPort
        }

        try {
            serialPort = org.winplus.serial.utils.SerialPort(File(path), baudRate, 0)
        } catch (e : Exception) {
            Log.d(TAG, "cannot open serial port")
        }

        if (serialPort != null) {
            try {
                outputStream = serialPort?.outputStream
                inputStream = serialPort?.inputStream
                readThread = ReadThread()
                readThread?.start()
                isOpen = true

                Log.d(TAG, "open serial $path success")
            } catch (e: IOException) {
                Log.d(TAG, "cannot open stream")
            }
        }

        return serialPort
    }

    fun close() {
        readThread?.interrupt()
        serialPort?.close()
        serialPort = null
        isOpen = false

        Log.d(TAG, "serial port close")
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
        val clean = hex.replace("\\s".toRegex(), "")

        val byteArray = Utils.hexStringToBytes(clean)
        send(byteArray)
    }

    fun setOnDataReceiveListener(listener: (data: ByteArray)->Unit) {
        onDataReceiveListener = listener
    }

    private inner class ReadThread : Thread() {
        override fun run() {
            super.run()
            while (!isInterrupted) {
                if (inputStream == null) return
                val buffer = ByteArray(512)
                val size = inputStream?.read(buffer) ?: 0
                if (size > 0) {
                    Log.d(TAG, "start -----------$size")
                    val b = Arrays.copyOfRange(buffer, 0, size)
                    Log.d(TAG, Utils.bytesToHexString(b))
                    Log.d(TAG, "end -----------$size")
                    onDataReceiveListener(b)
                }
            }
        }
    }

}