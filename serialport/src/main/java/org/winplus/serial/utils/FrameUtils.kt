package org.winplus.serial.utils

import android.util.Log
import org.winplus.serial.FP_CHECK_PASSWORD
import org.winplus.serial.FP_FRAME_HEAD

/**
 * Created by jess on 19-1-13.
 */
class FrameUtils {

    private val TAG = "指纹数据帧"

    private var mBuffer = byteArrayOf()
    private var mSize = 0

    private var mFrameHeadIndex = 0 // 帧头的index
    private var mChecksumHeaderIndex = 0 //帧头校验和index
    private var mChecksumDataIndex = 0 // 数据帧校验和index
    private var mRPCodeIndex = 0 // 响应码的index

    private var mCMD = byteArrayOf() // 命令
    private var mRPCode = byteArrayOf() // 响应代码号
    private var mData = byteArrayOf() // 数据

    var isFrame =false // 表示这段数据是否想要的
    var isChecksumHead = false // 校验帧头
    var isChecksumData = false // 校验数据
    var password = false // 校验密码

    private lateinit var mCallback: (ByteArray, ByteArray, ByteArray) -> Unit

    fun clearBuffer() {
        Log.d(TAG, "清空buffer")
        mBuffer = byteArrayOf()
        mSize = 0

        mFrameHeadIndex = 0
        mChecksumHeaderIndex = 0
        mChecksumDataIndex = 0
        mRPCodeIndex = 0

        mCMD = byteArrayOf()
        mRPCode = byteArrayOf()
        mData = byteArrayOf()

        isFrame = false
        isChecksumData = false
        isChecksumHead = false
        password = false
    }

    fun receiveData(byteArray: ByteArray, size: Int) {
        mBuffer += byteArray.sliceArray(IntRange(0, size -1))
        mSize += size

        parseFrame()
    }

    fun setListener(callback: (cmd: ByteArray, code: ByteArray, data: ByteArray) -> Unit) {
        mCallback = callback
    }

    private fun checkSum(data: ByteArray): Byte {
        var sum = 0
        data.forEach {
            sum += it
        }

        return (sum.inv() + 1).toByte()
    }

    private fun checkSumToInt(data: IntArray): Int {
        var sum = 0
        data.forEach {
            sum += it
        }

        return (sum.inv() + 1)
    }

    fun generateFrame(cmd: IntArray, data: IntArray): IntArray {
        var dataLength = cmd[cmd.lastIndex] + FP_CHECK_PASSWORD.size + (cmd.size - 1) + 1

        var dataFrame = intArrayOf(0x00, dataLength)
        var frameHeaderCheckSum = checkSumToInt(FP_FRAME_HEAD + dataFrame)
        var sendCheckSum = checkSumToInt(FP_CHECK_PASSWORD + cmd[0] + cmd[1] + data)

        var frame =
            FP_FRAME_HEAD + dataFrame + intArrayOf(frameHeaderCheckSum) + FP_CHECK_PASSWORD + cmd[0] + cmd[1] + data + intArrayOf(
                sendCheckSum
            )

        return frame
    }

    private fun parseFrame(){
        if (checkFrameHead() && checksumHead() && checkPassword() && checksumData() ) {
            parseCMD()
            parseCode()
            parseData()
        }
    }

    /**
     * 检查帧头
     */
    private fun checkFrameHead(): Boolean {
        if (isFrame) return true

        var headSize = FP_FRAME_HEAD.size
        if (mBuffer.size >= headSize) {
            for (i in 0 until mBuffer.size) {
                val endIdx = i + headSize
                if (endIdx > mBuffer.size -1) {
                    return false
                } else if (FP_FRAME_HEAD.toByteArray() contentEquals mBuffer.sliceArray(IntRange(0, endIdx -1)) ) {
                    Log.d(TAG, "是该数据帧了")
                    mFrameHeadIndex = i
                    isFrame = true
                    return true
                }
            }
        }

        return false
    }

    /**
     * 校验帧头和
     */
    private fun checksumHead(): Boolean {
        if (isChecksumHead)  return true

        if (mBuffer.size >= FP_FRAME_HEAD.size + 3) {
            val checkSum = mBuffer[mFrameHeadIndex + (FP_FRAME_HEAD.size+2)]
            val checkSumIndex =  mFrameHeadIndex + (FP_FRAME_HEAD.size+1)
            val byteArray = mBuffer.sliceArray(IntRange(mFrameHeadIndex, checkSumIndex))
            val cs = checkSum(byteArray)
            if (checkSum == cs) {
                Log.d(TAG, "校验头正确")
                isChecksumHead = true
                mChecksumHeaderIndex = checkSumIndex + 1

                return true
            }
        }

        return false
    }

    /**
     * 校验密码
     */
    private fun checkPassword(): Boolean {
        if (this.password) return true

        if (mChecksumHeaderIndex > 0 && mBuffer.size > (mChecksumHeaderIndex + 4)) {
            var password = mBuffer.sliceArray(IntRange(mChecksumHeaderIndex+1, mChecksumHeaderIndex+4))
            if (password contentEquals FP_CHECK_PASSWORD.toByteArray()) {
                Log.d(TAG, "密码正确")
                 this.password = true
                return true
            } else {
                Log.d(TAG, "校验密码错误")
            }
        }

        return false
    }

    /**
     * 解析命令
     */
    private fun parseCMD() {
        if (mCMD.isNotEmpty()) {
            return
        }

        var cmdIndex = mChecksumHeaderIndex +5
        if (this.password && cmdIndex + 1 < mBuffer.size) {
            mCMD = mBuffer.sliceArray(IntRange(cmdIndex, cmdIndex+1))
            Log.d(TAG, "命令: ${String.format("%02X %02X", mCMD[0], mCMD[1])}")
        }
    }

    /**
     * 解析响响应码
     */
    private fun parseCode() {
        if (mRPCode.isNotEmpty()) {
            return
        }

        var codeIndex = mChecksumHeaderIndex + 7
        if (codeIndex + 3 < mBuffer.size) {
            mRPCode = mBuffer.sliceArray(IntRange(codeIndex, codeIndex + 3))
            mRPCodeIndex = codeIndex
            Log.d(TAG, "响应码: ${String.format("%02X %02X %02X %02X", mRPCode[0], mRPCode[1], mRPCode[2], mRPCode[3])}")
        }
    }

    /**
     * 校验数据
     */
    private fun checksumData(): Boolean {
        var startIndex = mChecksumHeaderIndex + 1
        var endIndex = mChecksumHeaderIndex + 11
        if (endIndex < mBuffer.size) {
            for (i in endIndex until mBuffer.size) {
                val checksum = mBuffer[i]
                val cs = checkSum(mBuffer.sliceArray(IntRange(startIndex, i - 1)))

                if (checksum == cs) {
                    mChecksumDataIndex = i
                    Log.d(TAG, "校验数据正确")
                    return true
                }
            }
        }

        return false
    }

    /**
     * 解析数据，在解析响应码后调用
     */
    private fun parseData() {
        if (mChecksumDataIndex < mBuffer.size) {
            if (mRPCodeIndex + 4 == mChecksumDataIndex) {
                Log.d(TAG, "没有接收数据")

                mCallback(mCMD, mRPCode, byteArrayOf())
            } else {
                mData = mBuffer.sliceArray(IntRange(mRPCodeIndex+4, mChecksumDataIndex -1))
                var data = StringBuffer()
                mData.forEach {
                    data.append(String.format("%02X", it))
                    data.append(" ")
                }
                mCallback(mCMD,mRPCode, mData)

                Log.d(TAG, "接收的数据: ${data}")

            }
        }
    }

}