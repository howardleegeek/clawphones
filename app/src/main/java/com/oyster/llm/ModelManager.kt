package com.oyster.llm

import android.content.Context
import java.io.File
import java.net.URL
import java.util.concurrent.Executors

class ModelManager(private val context: Context) {
    private val cacheDir: File = File(context.cacheDir, "llm_models")
    private val executor = Executors.newFixedThreadPool(2)
    private val taskId = "modelmgr-${System.currentTimeMillis()}"

    sealed class DownloadResult {
        data class Success(val path: String) : DownloadResult()
        data class Progress(val percent: Int) : DownloadResult()
        data class Error(val message: String) : DownloadResult()
    }

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    fun getModelPath(modelName: String): String {
        return File(cacheDir, "$modelName.tflite").absolutePath
    }

    fun isModelCached(modelName: String): Boolean {
        return File(cacheDir, "$modelName.tflite").exists()
    }

    fun downloadModel(modelName: String, modelUrl: String, callback: (DownloadResult) -> Unit) {
        if (isModelCached(modelName)) {
            callback(DownloadResult.Success(getModelPath(modelName)))
            return
        }
        executor.execute {
            try {
                android.util.Log.d("ModelManager", "Downloading model: $modelName")
                val url = URL(modelUrl)
                val connection = url.openConnection()
                connection.connect()
                val totalSize = connection.contentLength
                val inputStream = connection.getInputStream()
                val outputFile = File(cacheDir, "$modelName.tflite")
                val outputStream = outputFile.outputStream()
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var downloadedSize = 0
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    downloadedSize += bytesRead
                    if (totalSize > 0) {
                        val percent = (downloadedSize * 100) / totalSize
                        callback(DownloadResult.Progress(percent))
                    }
                }
                outputStream.close()
                inputStream.close()
                android.util.Log.d("ModelManager", "Model downloaded: $modelName")
                callback(DownloadResult.Success(outputFile.absolutePath))
            } catch (e: Exception) {
                android.util.Log.e("ModelManager", "Download failed: ${e.message}")
                callback(DownloadResult.Error("Download failed: ${e.message}"))
            }
        }
    }

    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
        android.util.Log.d("ModelManager", "Cache cleared")
    }

    fun getCacheSize(): Long {
        return cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    }
}
