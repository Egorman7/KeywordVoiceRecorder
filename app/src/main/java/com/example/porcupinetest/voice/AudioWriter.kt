package com.example.porcupinetest.voice

import android.content.Context
import com.example.porcupinetest.voice.saver.RawFileWriter
import java.io.File

class AudioWriter(context: Context, private val sampleRate: Int){
    companion object{
        private const val WAVE_FILE = "audio_wave.wav"
    }
    private val rawFileWriter = RawFileWriter(context)

    val wavFile = File(context.filesDir, WAVE_FILE)

    init {
        if(wavFile.exists()){
            wavFile.delete()
            wavFile.createNewFile()
        }
    }

    val rawFile = rawFileWriter.rawAudioFile

    fun writeShortArray(shortArray: ShortArray){
        rawFileWriter.write(shortArray)
    }

    fun finalize(){
        rawFileWriter.finalize()
        WavHelper.rawToWave(sampleRate, rawFile, wavFile)
    }

}