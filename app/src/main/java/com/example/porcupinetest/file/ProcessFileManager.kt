package com.example.porcupinetest.file

import com.example.porcupinetest.getSuffixInt
import com.example.porcupinetest.remove
import java.io.File
import java.lang.Exception

class ProcessFileManager(private val processDir: File){
    companion object{
        private const val FILE_PREFIX = "process"
    }

    fun obtainRawBytes(bytes: ByteArray): Boolean{
        return try{
            createFile(getLastFileSuffix() + 1).writeBytes(bytes)
            true
        } catch (e: Exception){
            false
        }
    }

    private fun getListOfFiles(): List<File>{
        return processDir.listFiles()?.toList() ?: listOf()
    }

    private fun createFile(suffix: Int): File{
        return File(processDir, "${FILE_PREFIX}_$suffix").apply {
            createNewFile()
        }
    }

    private fun getLastFileSuffix(): Int{
        return getListOfFiles().maxBy { it.getSuffixInt() }?.getSuffixInt() ?: -1
    }

    fun removeFileWithSuffix(suffix: Int){
        getListOfFiles().firstOrNull { it.getSuffixInt() == suffix }?.remove()
    }
}