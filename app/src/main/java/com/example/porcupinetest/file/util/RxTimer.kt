package com.example.porcupinetest.file.util

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RxTimer {
    private var timerDisposable: Disposable? = null
    private var currentTime = 0

    fun start(time: Int, onRing:() -> Unit){
        currentTime = time
        timerDisposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.newThread())
            .subscribe({
                currentTime--
                if(currentTime <= 0){
                    timerDisposable?.dispose()
                    onRing()
                }
            },{
                Timber.e(it)
            })
    }

    fun stop(){
        timerDisposable?.dispose()
    }
}