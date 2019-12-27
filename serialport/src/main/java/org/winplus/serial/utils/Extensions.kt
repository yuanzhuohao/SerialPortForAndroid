package org.winplus.serial.utils

/**
 * Created by jess on 19-1-13.
 */
fun IntArray.toByteArray(): ByteArray {
    return this.foldIndexed(ByteArray(this.size)) { i, a, v -> a.apply { set(i, v.toByte()) } }
}

fun ByteArray.toIntArray(): IntArray {
    var intArray = intArrayOf()
    this.forEach {
        val hex = String.format("%02x", it)
        val int = Integer.parseInt(hex, 16)
        intArray += int
    }

    return intArray
}