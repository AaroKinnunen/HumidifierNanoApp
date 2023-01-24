package com.example.arduino_sense

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.system.exitProcess

var data = AppData()

class StartActivity : AppCompatActivity() {
    private lateinit var openLogin: Button
    //private lateinit var openSignup: Button
    private lateinit var usertext:TextView
    lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this,R.color.trans)))
        pref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
        setContentView(R.layout.start_activity)
        initOpenLogin()
        checkBLESupport()
        statusCheck()
        checkPermissions()
    }
    override fun onStart() {
        super.onStart()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
                val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBTIntent, 1)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        pref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
        //readUser()
        initOpenLogin()
        //initOpenSignup()
    }
    override fun onBackPressed(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to close the app?")
            .setCancelable(false)
            .setPositiveButton("Yes") {
                    _: DialogInterface?, _: Int ->  exitProcess(0)
            }
            .setNegativeButton("No") {
                    dialog: DialogInterface, _: Int -> dialog.cancel()
            }
        val alert = builder.create()
        alert.show()
    }
    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
    private fun checkBLESupport() {
        // Check if BLE is supported on the device.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toast("BLE not supported!")
            finish()
        }
    }
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                42
            )
        }
    }

    private fun statusCheck() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") {
                    _: DialogInterface?, _: Int -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            )
            }
            .setNegativeButton("No") {
                    dialog: DialogInterface, _: Int -> dialog.cancel()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun initOpenLogin() {
        openLogin = findViewById(R.id.btn_open_login)
        openLogin.setOnClickListener {
            loginOrConnect()
        }
        loginOrLogoutText()
    }




    private fun loginOrConnect(){
        //if (data.getToken().startsWith("Bearer")) {
            //finishAffinity()
            val intent = Intent(this@StartActivity, MainActivity::class.java)
            startActivity(intent)
       // }
        /*
        else {
            val intent = Intent(this@StartActivity, LoginActivity::class.java)
            startActivity(intent)
        }

         */
    }

    private fun loginOrLogoutText() {
        openLogin.setText("Start")
    }

}