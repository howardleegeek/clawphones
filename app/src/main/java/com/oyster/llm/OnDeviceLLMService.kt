package com.oyster.llm

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OnDeviceLLMService(
    private val context: Context,
    private val modelManager: ModelManager
) {
    private var interpreter: Interpreter? = null
    private var isModelLoaded = false
    private val taskId = "llm-${System.currentTimeMillis()}"

    sealed class LLMResult {
        data class Success(val response: String) : LLMResult()
        data class Error(val message: String, val code: Int) : LLMResult()
    }

    fun loadModel(modelName: String, callback: (LLMResult) -> Unit) {
        try {
            val modelPath = modelManager.getModelPath(modelName)
            if (!File(modelPath).exists()) {
                callback(LLMResult.Error("Model not found: $modelName", 404))
                return
            }
            val options = Interpreter.Options().apply {
                numThreads = 4
            }
            interpreter = Interpreter(File(modelPath), options)
            isModelLoaded = true
            android.util.Log.d("OnDeviceLLM", "Model loaded successfully: $modelName")
            callback(LLMResult.Success("Model loaded: $modelName"))
        } catch (e: Exception) {
            android.util.Log.e("OnDeviceLLM", "Failed to load model: ${e.message}")
            callback(LLMResult.Error("Failed to load model: ${e.message}", 500))
        }
    }

    fun infer(prompt: String, callback: (LLMResult) -> Unit) {
        if (!isModelLoaded || interpreter == null) {
            callback(LLMResult.Error("Model not loaded", 400))
            return
        }
        try {
            val inputBuffer = prepareInput(prompt)
            val outputBuffer = Array(1) { FloatArray(256) }
            interpreter?.run(inputBuffer, outputBuffer)
            val response = decodeOutput(outputBuffer[0])
            android.util.Log.d("OnDeviceLLM", "Inference completed for task: $taskId")
            callback(LLMResult.Success(response))
        } catch (e: Exception) {
            android.util.Log.e("OnDeviceLLM", "Inference failed: ${e.message}")
            callback(LLMResult.Error("Inference failed: ${e.message}", 500))
        }
    }

    private fun prepareInput(prompt: String): ByteBuffer {
        val inputSize = 512
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize)
        byteBuffer.order(ByteOrder.nativeOrder())
        val tokens = prompt.take(inputSize).map { it.code.toFloat() }
        tokens.forEachIndexed { index, token ->
            byteBuffer.putFloat(index, token)
        }
        return byteBuffer
    }

    private fun decodeOutput(output: FloatArray): String {
        return output.takeWhile { it > 0f }
            .map { it.toInt().toChar() }
            .joinToString("")
            .trim()
    }

    fun unloadModel() {
        interpreter?.close()
        interpreter = null
        isModelLoaded = false
        android.util.Log.d("OnDeviceLLM", "Model unloaded")
    }

    fun isModelReady(): Boolean = isModelLoaded
}
