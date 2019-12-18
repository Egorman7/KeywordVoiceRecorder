package com.example.porcupinetest.file

import com.example.porcupinetest.getSuffixInt
import com.example.porcupinetest.remove
import java.io.File
import java.nio.ByteBuffer

class RawAfterFileManager(private val rawDir: File,
                          private val maxFileSize: Int){
    companion object{
        private const val FILE_PREFIX = "after"
    }

    fun obtainBytes(bytes: ByteArray){
        getLastFile().appendBytes(bytes)
    }

    private fun getLastFile(): File{
        val list = getListOfFiles()
        if(list.isEmpty()){
            return createFile(1)
        }
        var file = File("")
        var lastSuffix = -1
        list.forEach { afterFile ->
            val suffix = afterFile.getSuffixInt()
            if(lastSuffix < suffix){
                lastSuffix = suffix
                file = afterFile
            }
        }
        return if(file.length() >= maxFileSize){
            createFile(++lastSuffix)
        } else {
            file
        }
    }

    fun getRawData(): ByteArray{
        val list = mutableListOf<ByteArray>()
        getListOfFiles().forEach { file ->
            list.add(file.readBytes())
        }
        val allocatedSize = list.sumBy { it.size }
        val buffer = ByteBuffer.allocate(allocatedSize)
        list.forEach { array ->
            buffer.put(array)
        }
        return buffer.array()
    }

    fun cleanUp(){
        getListOfFiles().forEach { file ->
            file.remove()
        }
    }

    private fun createFile(suffix: Int):File{
        return File(rawDir, "${FILE_PREFIX}_$suffix").apply {
            createNewFile()
        }
    }

    private fun getListOfFiles(): List<File>{
        return rawDir.listFiles()?.filter { it.name.contains(FILE_PREFIX) } ?: listOf()
    }
}