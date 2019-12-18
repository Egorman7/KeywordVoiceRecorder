package com.example.porcupinetest.voice.saver

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class RawFileWriter(context: Context) {
    companion object{
        private const val RAW_FILENAME = "audio_raw"
    }

    val rawAudioFile : File
    private val stream : FileOutputStream

    init {
        rawAudioFile = File(context.filesDir, RAW_FILENAME)
        if(rawAudioFile.exists()){
            rawAudioFile.delete()
            rawAudioFile.createNewFile()
        }
        stream = FileOutputStream(rawAudioFile)
    }

    fun write(shortArray: ShortArray){
        val buffer = ByteBuffer.allocate(shortArray.size * 2)
        for(short in shortArray){
            buffer.putShort(short)
        }
        stream.write(buffer.array())
    }

    fun finalize(){
        stream.flush()
        stream.close()
    }
}