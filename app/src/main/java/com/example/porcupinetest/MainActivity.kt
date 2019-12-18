package com.example.porcupinetest

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.porcupinetest.service.VoiceService
import com.example.porcupinetest.voice.BackgroundAudioPlayer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object{
        private const val REQUEST_PERMISSIONS_AUDIO = 123
    }
    private val connection = object: ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? VoiceService.Binder
            if(binder?.isServiceRunning() == true){
                main_button.text = "STOP"
            }
            binder?.getSampleRate()?.also { safeInt ->
                player = BackgroundAudioPlayer(this@MainActivity, safeInt)
            }
        }
    }

    private lateinit var player: BackgroundAudioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_button.setOnClickListener {
            if(main_button.text == "START") {
                requestVoicePermissions {
                    ContextCompat.startForegroundService(this, getServiceIntent())
                    bindService(getServiceIntent(), connection, Context.BIND_AUTO_CREATE)
                    main_button.text = "STOP"
                }
            } else {
                main_button.text = "START"
                unbindService(connection)
                stopService(getServiceIntent())
            }
        }
        main_play.setOnClickListener {
            if(::player.isInitialized){
                player.play()
            } else {
                Toast.makeText(this," No initialized!", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requestVoicePermissions {
            try{
                bindService(getServiceIntent(), connection, Context.BIND_AUTO_CREATE)
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }



    private var onSuccessAction: (() -> Unit)? = null
    private fun requestVoicePermissions(onSuccess: () -> Unit){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            onSuccessAction = onSuccess
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSIONS_AUDIO)
        } else {
            onSuccess()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_PERMISSIONS_AUDIO && grantResults.all { it == PackageManager.PERMISSION_GRANTED }){
            onSuccessAction?.invoke()
        }
    }

    private fun getServiceIntent(): Intent{
        return Intent(this, VoiceService::class.java)
    }

    override fun onStop() {
        try{
            unbindService(connection)
        } catch (ignored: Exception){}
        super.onStop()
    }
}
