package com.example.porcupinetest.file

import com.example.porcupinetest.convertToByte
import java.io.File
import java.nio.ByteBuffer

class RawFileManager(private val rawDir: File, maxFileSize: Int, maxFilePool: Int){

    private val beforeFileManager = RawBeforeFileManager(rawDir, maxFileSize, maxFilePool)
    private val afterFileManager = RawAfterFileManager(rawDir, maxFileSize)

    private var isKeywordObtained = false

    fun obtainKeyword(){
        isKeywordObtained = true
    }

    fun obtainShortArray(shortArray: ShortArray){
        val byteArray = shortArray.convertToByte()
        if(isKeywordObtained){
            afterFileManager.obtainBytes(byteArray)
        } else {
            beforeFileManager.obtainBytes(byteArray)
        }
    }

    fun getRawData(beforeDataSize: Int): ByteArray{
        val before = beforeFileManager.getRawData(beforeDataSize)
        val after = afterFileManager.getRawData()
        return ByteBuffer.allocate(before.size + after.size)
            .put(before)
            .put(after)
            .array()
    }

    fun cleanUp(){
        beforeFileManager.cleanUp()
        afterFileManager.cleanUp()
        isKeywordObtained = false
    }
}