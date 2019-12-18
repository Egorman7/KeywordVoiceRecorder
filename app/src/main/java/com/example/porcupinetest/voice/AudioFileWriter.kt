package com.example.porcupinetest.voice

import android.content.Context
import android.media.AudioFormat
import java.io.File
import java.io.OutputStream
import java.io.RandomAccessFile
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioFileWriter(private val context: Context,
                      private val channelMask: Int,
                      private val sampleRate: Int,
                      private val encoding: Int) {
    companion object{
        private const val TEMP_FILE_NAME = "temp_audio_file.wav"
    }
    private val audioFile = File(context.filesDir, TEMP_FILE_NAME)

    private val channels: Short = when(channelMask){
        AudioFormat.CHANNEL_IN_MONO -> 1
        AudioFormat.CHANNEL_IN_STEREO -> 2
        else -> throw IllegalArgumentException("Wrong channel mask")
    }

    private val bitDepth: Short = when(encoding){
        AudioFormat.ENCODING_PCM_8BIT -> 8
        AudioFormat.ENCODING_PCM_16BIT -> 16
        AudioFormat.ENCODING_PCM_FLOAT -> 32
        else -> throw IllegalArgumentException("Wrong encoding")
    }

    private val outputStream: OutputStream

    init {
        if(audioFile.exists()){
            audioFile.delete()
        }
        outputStream = audioFile.outputStream()
        writeWavHeader()
    }

    fun obtainBytes(bytes: ByteArray){
        outputStream.write(bytes)
    }

    fun finalize(){
        outputStream.flush()
        outputStream.close()
        updateWavHeader()
    }


    private fun writeWavHeader(){
        val littleBytes = ByteBuffer.allocate(14)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(channels)
            .putInt(sampleRate)
            .putInt(sampleRate*channels*(bitDepth/8))
            .putShort((channels*(bitDepth/8)).toShort())
            .putShort(bitDepth)
            .array()
        outputStream.write(WavHelper.completeHeader(littleBytes))
    }

    private fun updateWavHeader(){
        val sizes = ByteBuffer.allocate(8)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(audioFile.length().toInt() - 8)
            .putInt(audioFile.length().toInt() - 44)
            .array()
        var accessWave: RandomAccessFile? = null
        try{
            accessWave = RandomAccessFile(audioFile, "rw")
            accessWave.seek(4)
            accessWave.write(sizes, 0, 4)
            accessWave.seek(40)
            accessWave.write(sizes, 4, 4)
        } catch (e: Exception){

        } finally {
            accessWave?.close()
        }
    }
}