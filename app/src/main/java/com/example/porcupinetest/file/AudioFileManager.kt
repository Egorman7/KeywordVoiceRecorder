package com.example.porcupinetest.file

import android.content.Context
import com.example.porcupinetest.createDir
import com.example.porcupinetest.file.util.RxTimer
import timber.log.Timber
import java.io.File

class AudioFileManager(context: Context) {
    companion object{
        private const val TIME_PER_FILE = 10
        private const val SIZE_PER_SEC = 32768
        private const val MAX_FILE_SIZE = TIME_PER_FILE * SIZE_PER_SEC

        private const val DIR_RAW = "raw"
        private const val DIR_PROCESS = "process"
        private const val DIR_WAVE = "wave"
        private const val DIR_READY = "ready"

        //todo make it not constant
        private const val MAX_FILE_POOL = 3
        private const val BEFORE_DATA_SIZE = 15 * SIZE_PER_SEC
        private const val AFTER_TIME = 5
    }

    private val dirRaw = File(context.filesDir, DIR_RAW)
    private val dirProcess = File(context.filesDir, DIR_PROCESS)
    private val dirWave = File(context.filesDir, DIR_WAVE)
    private val dirReady = File(context.filesDir, DIR_READY)

    init {
        dirRaw.createDir()
        dirProcess.createDir()
        dirWave.createDir()
        dirReady.createDir()
    }

    private val rawFileManager = RawFileManager(dirRaw, MAX_FILE_SIZE, MAX_FILE_POOL)
    private val processFileManager = ProcessFileManager(dirProcess)
    private val waveFileManager = WaveFileManager(context, dirProcess, dirWave, dirReady)

    private val timer = RxTimer()
    private var isProcessPreparing = false

    var sampleRate = 8000

    fun obtainKeyword(){
        Timber.d("KEYWORD OBTAINED!")
        timer.start(AFTER_TIME){
            getRawData()
            isProcessPreparing = true
        }
        rawFileManager.obtainKeyword()
    }

    fun obtainRawShortArray(array: ShortArray){
        if(!isProcessPreparing) {
            rawFileManager.obtainShortArray(array)
        }
    }

    fun getRawData(){
        Timber.d("GETTING RAW DATA")
        if(processFileManager.obtainRawBytes(rawFileManager.getRawData(BEFORE_DATA_SIZE))){
            rawFileManager.cleanUp()
            isProcessPreparing = false
            waveFileManager.performProcessing(sampleRate)
        }
    }

    fun reset(){
        rawFileManager.cleanUp()
    }
}