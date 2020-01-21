package com.example.bluetoothapp.utils

import android.app.Application
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class Writer {
    companion object {
        fun writeFile(textToSave: String, filesDir:File, fileName: String) {
            try {


                File(filesDir.absolutePath+"/"+fileName).printWriter().use { out ->
                    out.print(textToSave)
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        fun readFile(filesDir:File,fileName: String): List<String> {
            try {
                return File(filesDir.absolutePath+"/"+fileName).readLines()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return listOf<String>()
            } catch (e: IOException) {
                e.printStackTrace()
                return listOf<String>()
            }
        }


    }
}