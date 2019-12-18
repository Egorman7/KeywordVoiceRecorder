package com.example.porcupinetest.voice

import ai.picovoice.porcupinemanager.PorcupineManager
import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import com.example.porcupinetest.R
import java.io.File

class PorcupineWrapper(private val context: Context) {

    var onRecognized: (() -> Unit)? = null
    var onBufferObtained: ((ShortArray) -> Unit)? = null
    private val manager by lazy {
        val model = File(context.filesDir, getResourceName(R.raw.porcupine_params, "pv"))
        val word = File(context.filesDir, getResourceName(R.raw.porcupine_android, "ppn"))
        PorcupineManager(model.absolutePath, word.absolutePath, 1f) { int ->
            Log.d("RECOGNIZER", "Int = $int")
            onRecognized?.invoke()
        }.apply {
            setCallback { buffer ->
//                Log.d("CALLBACK", buffer.map { it.toByte() }.toString())
                onBufferObtained?.invoke(buffer)
            }
        }
    }

    init {
        copyResources()
    }


    fun start() {
        manager.start()
    }

    fun stop() {
        manager.stop()
    }

    private fun copyResources() {
        val list = mapOf(
            R.raw.porcupine_params to "pv",
            R.raw.bumblebee to "ppn",
            R.raw.porcupine_android to "ppn"
        )
        list.keys.forEach { id ->
            val stream = context.resources.openRawResource(id)
            val file = File(context.filesDir, getResourceName(id, list[id]!!))
            if (!file.exists()) {
                file.createNewFile()
                file.writeBytes(stream.readBytes())
            }
        }
    }

    private fun getResourceName(id: Int, ext: String): String {
        return "${context.resources.getResourceName(id)}.${ext}".split("/")[1].also {
            Log.d("RESOURCE", it)
        }
    }

    fun getSampleRate(): Int {
        return manager.sampleRate
    }

}