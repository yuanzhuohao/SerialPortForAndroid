package org.winplus.serial

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import org.winplus.serial.databinding.MainBinding
import org.winplus.serial.utils.FrameUtils
import org.winplus.serial.utils.toByteArray
import org.winplus.serial.utils.toIntArray
import java.io.*

class SerialDemoActivity : SerialPortActivity() {
    var mReception: EditText? = null
    var clrbutton: Button? = null

    var setPowerbuttona: Button? = null
    var setUsbEnable: Button? = null
    var flags = 0

    private var mReceiveMsg = ""
    private lateinit var mBinding: MainBinding

    private val mFrameUtils = FrameUtils()

    private var mTime = 1
    private var mFPId = byteArrayOf()
    private var mFPScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.main)
        mReception = findViewById<View>(R.id.EditTextReception) as EditText
        val Emission = findViewById<View>(R.id.EditTextEmission) as EditText
        clrbutton = findViewById<View>(R.id.clear) as Button
        clrbutton?.setOnClickListener { mReception?.setText("") }

        Emission.setOnEditorActionListener { v, actionId, event ->
            var i: Int
            val t = v.text
            val text = CharArray(t.length)
            i = 0
            while (i < t.length) {
                text[i] = t[i]
                i++
            }
            try {
                mOutputStream.write(String(text).toByteArray())
                mOutputStream.write('\n'.toInt())
            } catch (e: IOException) {
                e.printStackTrace()
            }

            false
        }
        setPowerbuttona = findViewById<View>(R.id.send) as Button
        setPowerbuttona?.setOnClickListener {
            mReceiveMsg = ""

            sendData(intArrayOf(0x55,0x55,0x00,0x00,0x00,0x00,0x00, 0x00, 0x00,0x00,0x00,0x00,0x00,0x00, 0x00, 0x00, 0xFF, 0x03, 0xFD, 0xD4, 0x14, 0x01, 0x17,0x00))
        }
        setUsbEnable = findViewById<View>(R.id.power) as Button
        setUsbEnable?.setOnClickListener {
            flags = (flags + 1) % 2
            watchdog(flags)
        }
        read_power()

        setFPCallback()
    }

    override fun onDataReceived(buffer: ByteArray, size: Int) {
        runOnUiThread {
            val strreadString = dumpBytes(buffer, size)

            mReceiveMsg += strreadString
            mReception?.setText(mReceiveMsg)

            mFrameUtils.receiveData(buffer, size)
        }
    }


    protected fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    fun watchdog(flag: Int) {
        val file = File(
            "/sys/bus/platform/drivers/io_set_camera/io_set_camera.23/cam_io_en"
        )
        if (file.exists()) {
            try {
                val fos = FileOutputStream(file)
                if (flag == 1) {
                    fos.write('1'.toInt())
                    setUsbEnable?.text = "开"
                } else {
                    fos.write('0'.toInt())
                    setUsbEnable?.text = "关"
                }
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }

    fun read_power() {
        val file = File(
            "/sys/bus/platform/drivers/io_set_camera/io_set_camera.23/cam_io_en"
        )
        if (file.exists()) {
            try {
                val fos = FileInputStream(file)
                val flags = fos.read()
                Log.d("zhantaiming", "" + flags)
                if (flags == 49) {
                    setUsbEnable?.text = "开"
                } else {
                    setUsbEnable?.text = "关"
                }
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun sendData(startSmoke: IntArray) {
        mReceiveMsg = ""
        mFrameUtils.clearBuffer()

        val stringBuffer = StringBuffer()
        startSmoke.forEach {
            try {
                mOutputStream.write(it)
                stringBuffer.append(String.format("%02x ", it.toByte()))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        mBinding.EditTextEmission.setText(stringBuffer.toString())
    }


    fun dumpBytes(bytes: ByteArray, size: Int): String {
        var i: Int
        val sb = StringBuffer()
        var s: String? = null
        i = 0
        while (i < size) {

            s = String.format("%02x ", bytes[i])
            if (s!!.length < 2) {
                s = "0$s"
            }

            sb.append(s)
            i++
        }
        return sb.toString()
    }

    fun reEnroll(view: View) {
        mTime = 1
        sendData(mFrameUtils.generateFrame(FP_ENROLL_CMD, intArrayOf(mTime)))
    }


    fun enroll(view: View) {
        sendData(mFrameUtils.generateFrame(FP_ENROLL_CMD, intArrayOf(mTime)))
        mTime++
    }

    fun queryResult(view: View) {
        sendData(mFrameUtils.generateFrame(FP_QUERY_ENROLL_CMD, intArrayOf()))
    }

    fun save(view: View) {
        sendData(mFrameUtils.generateFrame(FP_SAVE_FP_CMD, mFPId.toIntArray()))
    }

    fun querySave(view: View) {
        sendData(mFrameUtils.generateFrame(FP_QUERY_SAVE_FP_CMD, intArrayOf()))
    }

    fun clearAll(view: View) {
        sendData(mFrameUtils.generateFrame(FP_CLEAR_FP_CMD, intArrayOf(0x01, 0x00, 0x01)))
    }

    fun queryClearAll(view: View) {
        sendData(mFrameUtils.generateFrame(FP_QUERY_CLEAR_FP_CMD, intArrayOf()))
    }

    fun clearFP(view: View) {
        if (mBinding.fpId.text.toString().isNotEmpty()) {
            val id = Integer.parseInt(mBinding.fpId.text.toString())
            sendData(mFrameUtils.generateFrame(FP_CLEAR_FP_CMD, intArrayOf(0x00, 0x00, id)))
        }
    }

    fun match(view: View) {
        sendData(mFrameUtils.generateFrame(FP_MATCH_FP_CMD, intArrayOf()))
    }

    fun queryMatch(view: View) {
        sendData(mFrameUtils.generateFrame(FP_QUERY_MATCH_FP_CMD, intArrayOf()))
    }

    fun fingerState(view: View) {
        sendData(mFrameUtils.generateFrame(FP_QUERY_FINGER_ON_CMD, intArrayOf()))
    }

    fun sleep(view: View) {
        sendData(mFrameUtils.generateFrame(FP_SLEEP_CMD, intArrayOf(0x00)))
    }

    fun heartRate(view: View) {
        sendData(mFrameUtils.generateFrame(FP_HEART_RATE_CMD, intArrayOf()))
    }

    fun exist(view: View) {
        if (mBinding.fpId.text.toString().isNotEmpty()) {
            val id = Integer.parseInt(mBinding.fpId.text.toString())
            sendData(mFrameUtils.generateFrame(FP_EXIST_CMD, intArrayOf(0x00, id)))
        }
    }

    private fun setFPCallback() {
        mFrameUtils.setListener { cmd, code, data ->
            if (cmd contentEquals FP_QUERY_ENROLL_CMD.sliceArray(
                    IntRange(
                        0,
                        FP_QUERY_ENROLL_CMD.lastIndex - 1
                    )
                ).toByteArray()
            ) {
                if (code contentEquals FP_SUCCESS_CODE.toByteArray()) {
                    mFPId = data.sliceArray(IntRange(0, 1))
                    mFPScore = data[data.lastIndex].toInt()

                    showToast("${mFPId[1]} 得分: ${mFPScore}")
                } else {
                    showToast("录入指纹失败")
                }
            } else if (cmd contentEquals FP_QUERY_SAVE_FP_CMD.sliceArray(
                    IntRange(
                        0,
                        FP_QUERY_SAVE_FP_CMD.lastIndex - 1
                    )
                ).toByteArray()
            ) {
                if (code contentEquals FP_SUCCESS_CODE.toByteArray()) {
                    mFPId = data.sliceArray(IntRange(0, 1))

                    showToast("${mFPId[1]} 保存成功")
                } else {
                    showToast("保存失败")
                }
            } else if (cmd contentEquals FP_QUERY_CLEAR_FP_CMD.sliceArray(
                    IntRange(
                        0,
                        FP_QUERY_CLEAR_FP_CMD.lastIndex - 1
                    )
                ).toByteArray()
            ) {
                if (code contentEquals FP_SUCCESS_CODE.toByteArray()) {

                    showToast("删除成功")
                } else {
                    showToast("删除失败")
                }
            } else if (cmd contentEquals FP_QUERY_FINGER_ON_CMD.sliceArray(IntRange(0, 1)).toByteArray()) {
                if (code contentEquals  FP_SUCCESS_CODE.toByteArray()) {
                    if (data.isNotEmpty()) {
                        val state = data[0]
                        showToast("${state}")
                    }
                }
            } else if (cmd contentEquals FP_QUERY_MATCH_FP_CMD.sliceArray(IntRange(0,1)).toByteArray()) {
                if (code contentEquals  FP_SUCCESS_CODE.toByteArray() && data.sliceArray(IntRange(0,1)) contentEquals byteArrayOf(0,1)) {
                    val id = data[data.lastIndex].toInt()
                    showToast("匹配成功 ${id}")
                } else {
                    showToast("匹配失败")
                }
            }else if (cmd contentEquals FP_HEART_RATE_CMD.sliceArray(IntRange(0,1)).toByteArray()) {
                if (code contentEquals FP_SUCCESS_CODE.toByteArray()) {
                }
            } else if (cmd contentEquals FP_EXIST_CMD.sliceArray(IntRange(0,1)).toByteArray()) {
                if (code contentEquals FP_SUCCESS_CODE.toByteArray()) {
                    val exist = data[0]
                    showToast("${if (exist.toInt() == 1) "存在" else "不存在"}")
                }
            }
        }
    }
}
