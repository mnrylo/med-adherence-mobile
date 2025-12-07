package com.medmon.mobile.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.OutputStream

class CsvLogger(private val context: Context) {

    private var outputStream: OutputStream? = null
    private var fileName: String = ""

    fun startNewFile(sessionId: String) {
        fileName = "imu_session_${sessionId}.csv"

        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.RELATIVE_PATH, "Download/IMU/")
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        outputStream = resolver.openOutputStream(uri!!)

        // Write header
        outputStream?.apply {
            write("timestamp,accX,accY,accZ,gyrX,gyrY,gyrZ\n".toByteArray())
        }

        Log.d("CsvLogger", "CSV file created: $fileName")
    }

    fun appendRow(
        timestamp: String,
        accX: Float, accY: Float, accZ: Float,
        gyrX: Float, gyrY: Float, gyrZ: Float
    ) {
        try {
            outputStream?.apply {
                val line = "$timestamp,$accX,$accY,$accZ,$gyrX,$gyrY,$gyrZ\n"
                write(line.toByteArray())
            }
        } catch (e: Exception) {
            Log.e("CsvLogger", "Error writing CSV row: ${e.message}", e)
        }
    }

    fun close() {
        try {
            outputStream?.close()
            Log.d("CsvLogger", "CSV closed: $fileName")
        } catch (e: Exception) {
            Log.e("CsvLogger", "Error closing CSV: ${e.message}", e)
        }
    }
}
