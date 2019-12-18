package com.example.porcupinetest.file

import com.example.porcupinetest.file.util.WavHelper
import timber.log.Timber
import java.io.File

class WaveFileManager(private val processDir: File, private val waveDir: File){
    companion object{
        private const val FILE_PREFIX = "wave"
        private const val FILE_EXT = ".wav"
    }

    fun performProcessing(sampleRate: Int){
        Timber.d("CONVERTING TO WAVE")
        processDir.listFiles()?.forEach { rawFile ->
            val waveFile = createFile(getLastSuffix() + 1)
            WavHelper.rawToWave(sampleRate, rawFile, waveFile)
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
}