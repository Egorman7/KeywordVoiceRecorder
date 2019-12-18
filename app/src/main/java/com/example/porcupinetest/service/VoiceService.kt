package com.example.porcupinetest.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.porcupinetest.MainActivity
import com.example.porcupinetest.R
import com.example.porcupinetest.file.AudioFileManager
import com.example.porcupinetest.voice.AudioFileWriter
import com.example.porcupinetest.voice.AudioWriter
import com.example.porcupinetest.voice.BackgroundRecorder
import com.example.porcupinetest.voice.PorcupineWrapper
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.nio.ByteBuffer


class VoiceService : Service(){
    private var isRunning = false
    private val binder = Binder()
    private val porcupine by lazy {PorcupineWrapper(this)}
    private var c = 0
    private val channelId by lazy {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel("PorcupineTestChannel", "My channel")
        } else {
            "PorcupineTestChannel"
        }
    }
    private val recorder by lazy { BackgroundRecorder().apply {
        onBufferRead = { buffer ->
                Log.d("RECORDER", buffer.toString())
            }
        }
    }

//    private lateinit var writer: AudioFileWriter

//    private lateinit var audioWriter: AudioWriter

    private lateinit var audioFileManager: AudioFileManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SERVICE", "onStartCommand")
        isRunning = true
        val pendingIntent = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java),0)
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Test")
            .setContentText("times = ${c}")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(123, notification)
        audioFileManager = AudioFileManager(this)
        porcupine.onRecognized = {
            audioFileManager.obtainKeyword()
            val notification2 = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Test")
                .setContentText("times = ${++c}")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build()
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(123, notification2)
        }
//        audioWriter = AudioWriter(this, porcupine.getSampleRate())
//        writer = AudioFileWriter(this, AudioFormat.CHANNEL_IN_MONO, porcupine.getSampleRate(), AudioFormat.ENCODING_PCM_16BIT)
        audioFileManager.sampleRate = porcupine.getSampleRate()
        porcupine.onBufferObtained = { buffer ->
//            writer.obtainBytes(shortToByte(buffer))
//            audioWriter.writeShortArray(buffer.also{
//                Log.d("BUFFER", "length = ${it.size}")
            audioFileManager.obtainRawShortArray(buffer)
        }
        porcupine.start()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class Binder: android.os.Binder(){
        fun isServiceRunning(): Boolean{
            return this@VoiceService.isRunning
        }

        fun getSampleRate(): Int{
            return porcupine.getSampleRate()
        }
    }

    var doOnDestroy: (() -> Unit)? = null
    override fun onDestroy() {
        isRunning = false
        doOnDestroy?.invoke()
//        writer.finalize()
        audioFileManager.reset()
//        audioWriter.finalize()
        porcupine.stop()
//        recorder.release()
//        recorder.stop()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(channelId: String, channelName: String): String{
        val c = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        c.lightColor = Color.BLUE
        c.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(c)
        return channelId
    }

    fun shortToByte(sData: ShortArray): ByteArray{
        val bytes = ByteBuffer.allocate(sData.size * 2)
        bytes.asShortBuffer().put(sData)
        return bytes.array()
    }

//    private fun short2byte(sData: ShortArray): ByteArray? {
//        val shortArrsize = sData.size
//        val bytes = ByteArray(shortArrsize * 2)
//        for (i in 0 until shortArrsize) {
//            bytes[i * 2] = (sData[i] and 0x00FF) as Byte
//            bytes[i * 2 + 1] = (sData[i] >> 8) as Byte
//            sData[i] = 0
//        }
//        return bytes
//    }


    private fun audioFile(): File = File(filesDir, "audio_temp.mp3")
}