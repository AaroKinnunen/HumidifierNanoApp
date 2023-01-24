package com.example.arduino_sense

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.example.arduino_sense.databinding.ControlRoomLayoutBinding
import kotlinx.coroutines.*
import java.util.*


class ControlRoom: AppCompatActivity() {
    private lateinit var limitButton: Button
    private lateinit var currentH: TextView
    private var bleController: BLEController? = null
    private lateinit var binding: ControlRoomLayoutBinding
    //private var dataService = DataService()
    private lateinit var job: Job
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_room_layout)
        bleController = BLEController.getInstance(this)
        //job = startRepeatingJob(300000) // 300000 5 minutes
        binding = DataBindingUtil.setContentView(this, R.layout.control_room_layout)
        binding.datas = data
        limitButton = findViewById(R.id.setbutton)
        currentH = findViewById(R.id.curHum)
        binding.autoButton.setOnClickListener { toggleMode() }
        binding.backbtn.setOnClickListener { goback() }

        binding.switchLed.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener() { buttonView, isChecked ->
            if (isChecked) {
                data.setLedMode(LedMode.ON)
            } else {
                data.setLedMode(LedMode.OFF)
            }
            bleController!!.sendLEDData(data.getLedMode().to_arduino)
        })

        limitButton.setOnClickListener {
            val input = binding.setLimit.text.toString()
            if (input.isNotEmpty()) {
                changeFanSpeed(input.toInt())
                //bleController!!.limitHum=input.toInt()
                bleController!!.readSpeed()
                currentH.setText("Current Minimum Humidity level: "+ input)
            }


        }

        initMode()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("tag", "control room onDestroy()")
        job.cancel()
    }

    private fun goback(){
        onBackPressed()
    }
/*
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

 */


    fun littleEndianConversion(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toInt() shl 8 * i)
        }
        return result
    }


    fun numberToByteArray (data: Number, size: Int = 4) : ByteArray =
        ByteArray (size) {i -> (data.toLong() shr (i*8)).toByte()}

    private fun initMode() {
        data.setMode(if (littleEndianConversion(bleController!!.getMode()) == 0) FanModes.AUTO else FanModes.USER)
        bleController!!.readSpeed()
        currentH.setText("Current Minimum Humidity level: "+ data.getSpeed().toString())
        setlimitVisibility()
        setSwitchVisibility()

    }

    private fun changeFanSpeed(speed: Int) {
        data.setSpeedUser(speed)
        bleController!!.sendSpeed(byteArrayOf(speed.toByte()))

    }

    private fun setSpeedBarVisibility() {
        //binding.speedBar.isEnabled = data.getMode() == FanModes.USER
    }
    private fun setSwitchVisibility() {
        binding.switchLed.isEnabled = data.getMode() == FanModes.USER
        binding.switchLed.isGone = data.getMode() == FanModes.AUTO
    }
    private fun setlimitVisibility() {
        binding.setbutton.isEnabled = data.getMode() == FanModes.AUTO
        binding.setLimit.isEnabled = data.getMode() == FanModes.AUTO
        binding.setbutton.isGone = data.getMode() == FanModes.USER
        binding.setLimit.isGone = data.getMode() == FanModes.USER
        currentH.isGone = data.getMode() == FanModes.USER
    }

    private fun turnFanOff() {
        data.setMode(FanModes.USER)
        data.setSpeedUser(0)
        bleController!!.sendMode(data.getMode().to_arduino)
        bleController!!.sendSpeed(byteArrayOf(0))
        setSpeedBarVisibility()
        data.setIsFanOfEnabled(false)
    }

    private fun toggleMode() {
        try {
            toast("Toggled mode to ${data.getMode().btn_text}")
            data.toggleMode()
            bleController!!.sendMode(data.getMode().to_arduino)
            bleController!!.sendSpeed(numberToByteArray(data.getSpeed()))
            bleController!!.readHum()
            if (data.getMode() == FanModes.USER){
                data.setLedMode(LedMode.OFF)
                bleController!!.sendLEDData(data.getLedMode().to_arduino)
            }
            setlimitVisibility()
            setSwitchVisibility()
        } catch (e: Exception){
            toast("try again $e")
        }
    }

//    private fun disconnectBle() {
//        toast("Disconnecting")
//        bleController!!.disconnect()
//        val intent = Intent(this@ControlRoom, MainActivity::class.java)
//        startActivity(intent)
//    }
    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}