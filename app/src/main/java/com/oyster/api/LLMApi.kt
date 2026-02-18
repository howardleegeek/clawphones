package com.oyster.api

interface LLMApi {
    fun generate(prompt: String, callback: (LLMResponse) -> Unit)
    fun loadModel(modelName: String, callback: (LLMResponse) -> Unit)
    fun isModelReady(): Boolean
    fun unload()
}

sealed class LLMResponse {
    data class Success(val text: String) : LLMResponse()
    data class Error(val message: String, val code: Int) : LLMResponse()
    data class Loading(val progress: Int) : LLMResponse()
}
