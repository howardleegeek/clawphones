package com.oyster.llm

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class OnDeviceLLMServiceTest {

    @Test
    fun testModelManagerCacheDirectory() {
        val tempDir = File.createTempFile("test", "")
        tempDir.delete()
        tempDir.mkdirs()
        
        val context = MockContext(tempDir)
        val modelManager = ModelManager(context)
        
        assertTrue(modelManager.getModelPath("test-model").contains("test-model.tflite"))
        assertFalse(modelManager.isModelCached("test-model"))
        
        tempDir.deleteRecursively()
    }

    @Test
    fun testModelManagerCaching() {
        val tempDir = File.createTempFile("test", "")
        tempDir.delete()
        tempDir.mkdirs()
        
        val context = MockContext(tempDir)
        val modelManager = ModelManager(context)
        
        val modelFile = File(tempDir, "llm_models/test-model.tflite")
        modelFile.parentFile?.mkdirs()
        modelFile.writeText("mock model")
        
        assertTrue(modelManager.isModelCached("test-model"))
        assertEquals(0L, modelManager.getCacheSize())
        
        modelFile.delete()
        tempDir.deleteRecursively()
    }

    @Test
    fun testLLMResultTypes() {
        val successResult = OnDeviceLLMService.LLMResult.Success("response")
        assertTrue(successResult is OnDeviceLLMService.LLMResult.Success)
        
        val errorResult = OnDeviceLLMService.LLMResult.Error("error", 500)
        assertTrue(errorResult is OnDeviceLLMService.LLMResult.Error)
    }

    @Test
    fun testDownloadResultTypes() {
        val progressResult = ModelManager.DownloadResult.Progress(50)
        assertTrue(progressResult is ModelManager.DownloadResult.Progress)
        
        val successResult = ModelManager.DownloadResult.Success("/path/model.tflite")
        assertTrue(successResult is ModelManager.DownloadResult.Success)
        
        val errorResult = ModelManager.DownloadResult.Error("failed")
        assertTrue(errorResult is ModelManager.DownloadResult.Error)
    }

    @Test
    fun testModelPathGeneration() {
        val tempDir = File.createTempFile("test", "")
        tempDir.delete()
        tempDir.mkdirs()
        
        val context = MockContext(tempDir)
        val modelManager = ModelManager(context)
        
        val path = modelManager.getModelPath("llama-2-7b")
        assertTrue(path.endsWith("llama-2-7b.tflite"))
        
        tempDir.deleteRecursively()
    }

    private class MockContext(private val cacheDir: File) : android.content.Context {
        override fun getCacheDir(): File = cacheDir
        override fun getApplicationContext(): android.content.Context = this
    }
}
