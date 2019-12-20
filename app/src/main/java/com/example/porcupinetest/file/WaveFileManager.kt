package com.example.porcupinetest.file

import android.content.Context
import cafe.adriel.androidaudioconverter.AndroidAudioConverter
import cafe.adriel.androidaudioconverter.callback.IConvertCallback
import cafe.adriel.androidaudioconverter.model.AudioFormat
import com.example.porcupinetest.file.util.WavHelper
import com.example.porcupinetest.remove
import timber.log.Timber
import java.io.File
import java.lang.Exception

class WaveFileManager(private val context: Context,
                      private val processDir: File,
                      private val waveDir: File,
                      private val readyDir: File){
    companion object{
        private const val FILE_PREFIX = "wave"
        private const val FILE_EXT = ".wav"

        private const val MP3_PREFIX = "audio"
        private const val MP3_EXT = ".mp3"
    }

    fun performProcessing(sampleRate: Int){
        Timber.d("CONVERTING TO WAVE")
        processDir.listFiles()?.forEach { rawFile ->
            val waveFile = createFile(getLastSuffix() + 1)
            WavHelper.rawToWave(sampleRate, rawFile, waveFile)
            AndroidAudioConverter.with(context)
                .setFile(waveFile)
                .setFormat(AudioFormat.MP3)
                .setCallback(object: IConvertCallback{
                    override fun onSuccess(convertedFile: File) {
                        saveMp3File(waveFile, convertedFile)
                    }

                    override fun onFailure(error: Exception?) {
                        Timber.e(error)
                    }
                }).convert()
            rawFile.remove()
        }
    }

    private fun getLastSuffix(): Int{
        return getListOfFiles().size
    }

    private fun getListOfFiles(): List<File>{
        return waveDir.listFiles()?.toList() ?: listOf()
    }

    private fun createFile(suffix: Int): File{
        return File(waveDir, "${FILE_PREFIX}_${suffix}$FILE_EXT").apply {
            createNewFile()
        }
    }

    private fun saveMp3File(wave: File, mp3: File){
        val target = createMp3File(getMp3Files().size + 1)
        mp3.copyTo(target, true)
        wave.remove()
    }

    private fun getMp3Files(): List<File>{
        return readyDir.listFiles()?.toList() ?: listOf()
    }

    private fun createMp3File(suffix: Int): File{
        return File(readyDir, "${MP3_PREFIX}_${suffix}$MP3_EXT").apply {
            createNewFile()
        }
    }
}