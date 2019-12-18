package com.example.porcupinetest.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import java.io.File
import java.nio.ByteBuffer
import java.nio.ShortBuffer

class BackgroundAudioPlayer(private val context: Context, private val sampleRate: Int) {

    var active = false
    fun play(boolean: Boolean){
        Thread{
            val bytes = audioFile().readBytes().map { it.toShort() }.toShortArray()
            val samples = ShortBuffer.wrap(bytes)
            var bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT)
            if(bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE){
                bufferSize = sampleRate * 2
            }
            val track = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM)
            track.play()
            track.registerStreamEventCallback({

            },object : AudioTrack.StreamEventCallback(){
                override fun onPresentationEnded(track: AudioTrack) {
                    active = false
                }
            })
            active = true
            while (active) {
                track.write(bytes, 0, bytes.size)
            }
        }
    }

    fun play(){
        Thread{
            val bytes = audioFile().readBytes()
            val samples = ByteBuffer.wrap(bytes)
            var bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT)
            if(bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE){
                bufferSize = sampleRate * 2
            }
            val track = /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) AudioTrack(AudioAttributes.Builder()
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED).build(),
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(), bufferSize, AudioTrack.MODE_STREAM, 0) else {*/
                AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM)
//            }
            track.play()

            val buffer = ByteArray(bufferSize)
            samples.rewind()
            val limit = samples.capacity()
            var total = 0
            while(samples.position() < limit){
                val left = limit - samples.position()
                var samplesToWrite = 0
                if(left >= buffer.size){
                    samples.get(buffer)
                    samplesToWrite = buffer.size
                } else {
                    for(i in left until buffer.size){
                        buffer[i] = 0
                    }
                    samples.get(buffer, 0, left)
                    samplesToWrite = left
                }
                total =+ samplesToWrite
                track.write(buffer, 0, samplesToWrite)
            }
            track.release()
        }.start()
    }

    fun audioFile(): File = File(context.filesDir, "audio_temp.mp3")
}