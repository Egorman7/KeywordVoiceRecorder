package com.example.porcupinetest

import java.io.File
import java.lang.Exception
import java.nio.ByteBuffer

fun File.createDir(){
    if(!exists()){
        mkdirs()
    } else {
        if(!isDirectory){
            delete()
            mkdirs()
        }
    }
}

fun File.remove(){
    if(exists()){
        delete()
    }
}

fun File.getSuffixInt(): Int{
    val splitted = nameWithoutExtension.split("_")
    return splitted[1].toSafeInt()
}

fun String.toSafeInt():Int{
    return try{
        toInt()
    } catch (e: Exception){
        -1
    }
}

fun Float.toBiggerInt(): Int{
    val int = this.toInt()
    return if(this > int){
        (int + 1)
    } else {
        int
    }
}

fun ShortArray.convertToByte(): ByteArray{
    val buffer = ByteBuffer.allocate(size * 2)
    for(short in this){
        buffer.putShort(short)
    }
    return buffer.array()
}