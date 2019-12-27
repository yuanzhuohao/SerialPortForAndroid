package tech.zhuohao.serialprotrroject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.winplus.serial.Serialer
import org.winplus.serial.utils.Utils

class MainActivity : AppCompatActivity() {

    private var serialPort = Serialer()

    private var device = ""
    private var baudRate = 0

    private var baudRates = arrayOf(9600, 115200)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        serialPort.open("/dev/ttyS1", 115200)

        serialPort.setOnDataReceiveListener {
            runOnUiThread {
                var text = tv_receive.text.toString() + "\n"
                text += Utils.bytesToHexString(it)

                tv_receive.text = text
            }
        }

        initView()
    }

    private fun initView() {
        val items = serialPort.getAllDevicePath()
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, items)
        spinner1.adapter = adapter

        val adapter2 = ArrayAdapter<Int>(this, android.R.layout.simple_dropdown_item_1line, baudRates)
        spinner2.adapter = adapter2

        spinner1.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                device = items[position]

                close()
            }
        }

        spinner2.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                baudRate = baudRates[position]

                close()
            }
        }

        initText()
    }

    private fun initText() {
        tv_state.text = if (serialPort.isOpen()) "串口状态: 开启"  else  "串口状态: 关闭"
        tv_state.setTextColor(ContextCompat.getColor(this,
            if (serialPort.isOpen()) android.R.color.holo_green_light else android.R.color.holo_red_light))

        btn_state.text = if (serialPort.isOpen()) "关闭" else "开启"
    }

    private fun close() {
        serialPort.close()

        initText()
    }

    fun send(view: View) {
//        Toast.makeText(this,"已发送", Toast.LENGTH_SHORT).show()
        serialPort.sendHex(et_data.text.toString())
        tv_receive.text = ""
    }

    fun open(view: View) {
        serialPort.open(device, baudRate)

        initText()
    }
}
