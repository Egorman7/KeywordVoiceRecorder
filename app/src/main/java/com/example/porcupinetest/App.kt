package com.example.porcupinetest

import android.app.Application
import cafe.adriel.androidaudioconverter.AndroidAudioConverter
import cafe.adriel.androidaudioconverter.callback.ILoadCallback
import timber.log.Timber
import java.io.File
import java.lang.Exception

class App : Application(){
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AndroidAudioConverter.load(this, object: ILoadCallback{
            override fun onSuccess() {
                val f = File(filesDir, "mp3e")
                if(!f.exists()) f.mkdirs()
            }

            override fun onFailure(error: Exception?) {
                Timber.e(error)
            }
        })
    }
}