package com.example.porcupinetest.file

import com.example.porcupinetest.getSuffixInt
import com.example.porcupinetest.remove
import com.example.porcupinetest.toBiggerInt
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer

class RawBeforeFileManager(private val rawDir: File,
                           private val maxFileSize: Int,
                           private val maxFilePool: Int){
    companion object{
        private const val FILE_PREFIX = "before"
    }

    fun obtainBytes(bytes: ByteArray){
        getLastOrNewFile().appendBytes(bytes)
    }

    private fun getLastOrNewFile(): File{
        val list = getListOfFiles()
        if(list.isEmpty()){
            return createFile(1)
        }
        var file = File("")
        var lastSuffix = -1
        list.forEach { beforeFile ->
            val suffix = beforeFile.getSuffixInt()
            if(suffix > lastSuffix){
                lastSuffix = suffix
                file = beforeFile
            }
        }
        return if(file.length() >= maxFileSize){
            if(list.size >= maxFilePool){
                list.first { it.getSuffixInt() == 1 }.remove()
                getListOfFiles().forEach {
                    it.renameTo(File(rawDir, "${FILE_PREFIX}_${it.getSuffixInt()-1}"))
                }
                createFile(lastSuffix)
            } else {
                createFile(++lastSuffix)
            }
        } else {
            file
        }
    }

    fun getRawData(dataSize: Int): ByteArray{
        val fileCountRaw = dataSize/maxFileSize.toFloat()
        val fileCount = fileCountRaw.toBiggerInt()
        val lastFileSize = (maxFileSize * (fileCountRaw - fileCountRaw.toInt())).toInt()
        val byteArrayList = if(fileCount <= maxFilePool) {
            // if file pool size is enough, than obtain from concrete byte pos
            getByteArrayList(fileCount, lastFileSize)
        } else {
            // else read all files
            val list = mutableListOf<ByteArray>()
            getListOfFiles().forEach { file ->
                list.add(file.readBytes())
            }
            list
        }
        val allocatedSize = byteArrayList.sumBy { it.size }
        val byteBuffer = ByteBuffer.allocate(allocatedSize)
        byteArrayList.forEach { byteArray ->
            byteBuffer.put(byteArray)
        }
        return byteBuffer.array()
    }

    private fun getByteArrayList(fileCount: Int, lastFileSize: Int): List<ByteArray>{
        val list = mutableListOf<ByteArray>()
        val fileList = getListOfFiles()
        val totalFiles = fileList.size
        val firstFileIndex = totalFiles - fileCount
//        val firstFile = RandomAccessFile(fileList[firstFileIndex], "rw")
//        firstFile.seek(step)
        val firstFile = fileList[firstFileIndex]
        val step = firstFile.length() - lastFileSize
        val firstData = firstFile.readBytes()
        val croppedData = firstData.filterIndexed { index, byte ->
            index >= step
        }.toByteArray()
        list.add(croppedData)
        for(i in (firstFileIndex+1) until totalFiles){
            list.add(fileList[i].readBytes())
        }
        return list
    }

    private fun createFile(suffix: Int): File{
        return File(rawDir, "${FILE_PREFIX}_$suffix").apply {
            createNewFile()
        }
    }

    private fun getListOfFiles(): List<File>{
        return rawDir.listFiles()?.filter { it.name.contains(FILE_PREFIX) } ?: listOf()
    }

    fun cleanUp(){
        getListOfFiles().forEach { file ->
            file.remove()
        }
    }
}