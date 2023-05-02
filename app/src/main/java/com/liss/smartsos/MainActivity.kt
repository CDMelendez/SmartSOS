package com.liss.smartsos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

//importaciones relacionadas a la conectividad bluetooth
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket

//importaciones necesarias
import android.widget.Button
import android.widget.Toast
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatDelegate
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.gsm.SmsManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Forzar que el modo oscuro se deshabilite
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        //Solicitar permisos iniciales
        if (!isSmsPermissionGranted()) {
            showPermissionDialog()
        } else {
            // El permiso ya ha sido concedido
        }

        //Boton de prueba para ejecutar la aplicacion 911
        val buttonTest = findViewById<Button>(R.id.button)
        buttonTest.setOnClickListener {
            //Ejecuta la funcion al pulsar el boton
            //exec911()
            //sendSMS("6645333103", "Mensaje de prueba",this)
            sendSMS("6641873545", "Mensaje de prueba 123 hola",this)
        }

        //Serial
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // El dispositivo no admite Bluetooth
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                // El Bluetooth no está activado, se puede solicitar al usuario que lo active
            } else {
                // El Bluetooth está activado
                val device = bluetoothAdapter.getRemoteDevice("98:D3:31:F5:B8:2E")
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                val socket = device.createRfcommSocketToServiceRecord(uuid)
                socket.connect()

                val inputStream = socket.inputStream
                val buffer = ByteArray(1024)
                var bytes: Int

                while (true) {
                    bytes = inputStream.read(buffer)
                    val message = String(buffer, 0, bytes)
                    runOnUiThread {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        if (message == "1")
                        {
                            exec911()
                            sendSMS("6642867484", "Mensaje de prueba 123",this)
                        }
                    }
                }
                socket.close()
            }
        }

        //BT
        /*
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Bluetooth no está habilitado", Toast.LENGTH_LONG).show()
            return
        }

        bluetoothAdapter.getProfileProxy(this, mHeadsetServiceListener, BluetoothProfile.HEADSET)

        */

        //TEST
    }

    //Solicitando permisos
    private val PERMISSION_REQUEST_SEND_SMS = 123 // número de solicitud de permiso
    // Función para verificar si se tiene permiso para enviar SMS
    private fun isSmsPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    // Función para solicitar permiso para enviar SMS
    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), PERMISSION_REQUEST_SEND_SMS)
    }

    // Función para mostrar un diálogo de permisos
    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Esta aplicación necesita los permisos siguientes para funcionar correctamente.")
            setPositiveButton("Aceptar") { _, _ ->
                requestSmsPermission()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    //Ejecutar aplicacion 911
    private fun exec911() {
        //Nombre interno de la aplicacion 911
        val launchIntent = packageManager.getLaunchIntentForPackage("com.c4bc.alerta066m")
        if (launchIntent != null) {
            launchIntent.setClassName("com.c4bc.alerta066m", "com.c4bc.alerta066m.activities.Splash")
            startActivity(launchIntent)
        } else {
            //Envia mensaje cuando no se pueda encontrar la aplicacion
            Toast.makeText(this@MainActivity, "La aplicacion no se encuentra o no esta instalada en el dispositivo", Toast.LENGTH_LONG).show()
        }
    }

    //Ejecutar aplicacion MedTrak
    private fun execMedTrack() {
        //Nombre interno de la aplicacion MedTrack
        val launchIntent2 = packageManager.getLaunchIntentForPackage("med.track.med")
        if (launchIntent2 != null) {
            launchIntent2.setClassName("med.track.med", "io.ionic.starter.MainActivity")
            startActivity(launchIntent2)
        } else {
            //Envia mensaje cuando no se pueda encontrar la aplicacion
            Toast.makeText(this@MainActivity, "La aplicacion no se encuentra o no esta instalada en el dispositivo", Toast.LENGTH_LONG).show()
        }
    }

    //Mostrar el boton de configuracion
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings,menu)
        return true
    }

    //Detectar pulsaciones en el boton de configuracion
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                //Acciones a realizar al presionar el boton
                Toast.makeText(this, "Boton de configuracion presionado", Toast.LENGTH_LONG).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    //Enviar SMS
    private fun sendSMS(phoneNumber: String, message: String, context: Context) {
        // Comprobamos si tenemos permiso para enviar SMS
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            // Si no tenemos permiso, mostramos un mensaje al usuario
            Toast.makeText(context, "Se requiere permiso para enviar SMS", Toast.LENGTH_SHORT).show()
            return
        }

        // Creamos el PendingIntent con la flag FLAG_IMMUTABLE
        val sentIntent = Intent("SMS_SENT").let { sentIntent ->
            PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_IMMUTABLE)
        }

        // Enviamos el mensaje
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, null)
    }

    /*
    //Codificacion relacionada con la conectividad Bluetooth
    private var mBluetoothHeadset: BluetoothHeadset? = null
    private val mHeadsetServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, headset: BluetoothProfile) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = headset as BluetoothHeadset
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = null
            }
        }
    }
    */

    override fun onDestroy() {
        super.onDestroy()
        /*
        mBluetoothHeadset?.let {
            BluetoothAdapter.getDefaultAdapter()?.closeProfileProxy(BluetoothProfile.HEADSET, it)
        }
        */
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (event != null && event.action == KeyEvent.ACTION_DOWN) {
            Toast.makeText(this, "KeyCode: $keyCode", Toast.LENGTH_SHORT).show()
            //return true
        }

        when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_START-> {
                Toast.makeText(this, "Pulsación de boton detectada", Toast.LENGTH_LONG).show()
                exec911()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                Toast.makeText(this, "Pulsación de bluetooth detectada", Toast.LENGTH_LONG).show()
                exec911()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY-> {
                Toast.makeText(this, "Pulsación de bluetooth detectada", Toast.LENGTH_LONG).show()
                exec911()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                Toast.makeText(this, "Pulsación de bluetooth detectada", Toast.LENGTH_LONG).show()
                exec911()
                return true
            }
            KeyEvent.KEYCODE_BUTTON_SELECT -> {
                Toast.makeText(this, "Pulsación de boton detectada", Toast.LENGTH_LONG).show()
                execMedTrack()
                return true
            }
            KeyEvent.KEYCODE_1 -> {
                Toast.makeText(this, "Pulsación de boton detectada", Toast.LENGTH_LONG).show()
                exec911()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}