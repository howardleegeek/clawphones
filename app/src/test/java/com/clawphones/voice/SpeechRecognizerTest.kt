package com.clawphones.voice

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.Locale

class SpeechRecognizerTest {

  // Minimal fake engine to intercept language and events
  class FakeSpeechEngine : SpeechEngine {
    var internalListener: RecognitionListener? = null
    var startedLanguageTag: String? = null
    override fun setRecognitionListener(l: RecognitionListener) { internalListener = l }
    override fun startListening(languageTag: String) { startedLanguageTag = languageTag }
    override fun stopListening() {}
    override fun destroy() {}
  }

  @Test
  fun enUSLanguageTagIsEnUS() {
    val ctx = mock(Context::class.java)
    val engine = FakeSpeechEngine()
    val calledListener = object : SpeechEventListener {
      override fun onPartial(text: String) {}
      override fun onResult(text: String) {}
      override fun onError(errorCode: Int) {}
    }
    val mgr = SpeechRecognizerManager(ctx, calledListener, engineFactory = { _ -> engine })
    mgr.startListening(listOf(Locale.US))
    assertEquals("en_US", engine.startedLanguageTag)
  }

  @Test
  fun zhCNLanguageTagIsZhCN() {
    val ctx = mock(Context::class.java)
    val engine = FakeSpeechEngine()
    val listener = object : SpeechEventListener {
      override fun onPartial(text: String) {}
      override fun onResult(text: String) {}
      override fun onError(errorCode: Int) {}
    }
    val mgr = SpeechRecognizerManager(ctx, listener, engineFactory = { _ -> engine })
    mgr.startListening(listOf(Locale.CHINA))
    assertEquals("zh_CN", engine.startedLanguageTag)
  }

  @Test
  fun recognitionFlowPartialAndFinal() {
    val ctx = mock(Context::class.java)
    val engine = FakeSpeechEngine()
    var lastPartial: String? = null
    var lastResult: String? = null
    val listener = object : SpeechEventListener {
      override fun onPartial(text: String) { lastPartial = text }
      override fun onResult(text: String) { lastResult = text }
      override fun onError(errorCode: Int) {}
    }
    val mgr = SpeechRecognizerManager(ctx, listener, engineFactory = { _ -> engine })
    mgr.startListening(listOf(Locale.US))
    val recListener = engine.internalListener
    // Simulate partial results
    val partialBundle = Bundle().apply { putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf("he")) }
    recListener?.onPartialResults(partialBundle)
    assertEquals("he", lastPartial)
    // Simulate final results
    val finalBundle = Bundle().apply { putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf("hello world")) }
    recListener?.onResults(finalBundle)
    assertEquals("hello world", lastResult)
  }
}
