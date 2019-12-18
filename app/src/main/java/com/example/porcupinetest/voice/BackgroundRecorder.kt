package com.example.porcupinetest.voice

import android.app.Application
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.io.RandomAccessFile
import java.lang.Exception

class BackgroundRecorder(){
    companion object{
        private const val TIMER_INTERVAL = 120
    }
    enum class State{
        INITIALIZING,
        READY,
        RECORDING,
        ERROR,
        STOPPED,
        IDLE
    }
    private var state: State = State.IDLE


    private lateinit var audioBuffer: ByteArray
    private val source: Int = MediaRecorder.AudioSource.MIC
    private var sampleRate = 0
    private var encoder = 0
    private var nChannels = 0
    private var bufferRead = 0
    private var bufferSize = 0
    private var tempAudioFile: RandomAccessFile? = null
    lateinit var audioRecorder: AudioRecord
    private val bSamples: Short = 16
    private var framePeriod = 0

    @Volatile
    private var recordThread: Thread? = null

    @Volatile
    var onBufferRead: ((ByteArray) -> Unit)? = null

    var timeStamp = 0
    var count = 0
    var preTimeStamp = 0

    private val updateListener = object: AudioRecord.OnRecordPositionUpdateListener{
        override fun onMarkerReached(recorder: AudioRecord?) {}
        override fun onPeriodicNotification(recorder: AudioRecord?) {}
    }

    init {
        sampleRate = 11025
        encoder = AudioFormat.ENCODING_PCM_16BIT
        nChannels = AudioFormat.CHANNEL_IN_MONO
        preTimeStamp = System.currentTimeMillis().toInt()
        try {
            framePeriod = sampleRate* TIMER_INTERVAL/1000
            bufferSize = framePeriod * 2 * bSamples * nChannels/8
            if(bufferSize < AudioRecord.getMinBufferSize(sampleRate, nChannels, encoder)){
                bufferSize = AudioRecord.getMinBufferSize(sampleRate, nChannels, encoder)
                framePeriod = bufferSize/(2*bSamples*nChannels/8)
            }
            audioRecorder = AudioRecord(source, sampleRate, nChannels, encoder, bufferSize)
            audioBuffer = ByteArray(2048)
            audioRecorder.setRecordPositionUpdateListener(updateListener)
            audioRecorder.positionNotificationPeriod = framePeriod
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun start(){
        if(state == State.INITIALIZING || state == State.IDLE){
            audioRecorder.startRecording()
            state = State.RECORDING
            recordThread = Thread{
                audioRecorder.read(audioBuffer,0, 2048)
                onBufferRead?.invoke(audioBuffer)
            }
            recordThread?.priority = Thread.MAX_PRIORITY
            recordThread?.start()
        } else {
            state = State.ERROR
            Log.e(this::class.java.simpleName, "start() called when recorder is in incorrect state!")
        }
    }

    fun stop(){
        if(state == State.RECORDING){
            audioRecorder.stop()
            recordThread?.interrupt()
            recordThread = null
            count = 0
            state = State.STOPPED
        } else {
            state = State.ERROR
            Log.e(this::class.java.simpleName, "stop() called when recorder is in incorrect state!")
        }
    }

    fun release(){
        if(state == State.RECORDING){
            stop()
        }
        if(::audioRecorder.isInitialized){
            audioRecorder.release()
        }
    }

    fun reset(){
        try {
            if (state != State.ERROR) {
                release()
            }
        } catch (e: Exception){
            Log.e(this::class.java.simpleName, e.message)
            state = State.ERROR
        }
    }
}