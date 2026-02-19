package com.clawphones.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import java.util.Locale

// Listener for speech events exposed to the UI layer
interface SpeechEventListener {
  fun onPartial(text: String)
  fun onResult(text: String)
  fun onError(errorCode: Int)
}

// Abstraction over the platform speech engine to enable unit testing
interface SpeechEngine {
  fun startListening(languageTag: String)
  fun stopListening()
  fun destroy()
  fun setRecognitionListener(listener: RecognitionListener)
}

// Android implementation of the SpeechEngine using android.speech APIs
class AndroidSpeechEngine(private val context: Context) : SpeechEngine {
  private var recognizer: SpeechRecognizer? = null
  private var registeredListener: RecognitionListener? = null

  override fun setRecognitionListener(listener: RecognitionListener) {
    registeredListener = listener
    if (recognizer == null) {
      recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    }
    recognizer?.setRecognitionListener(listener)
  }

  override fun startListening(languageTag: String) {
    if (recognizer == null) {
      recognizer = SpeechRecognizer.createSpeechRecognizer(context)
      recognizer?.setRecognitionListener(registeredListener)
    }
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }
    recognizer?.startListening(intent)
  }

  override fun stopListening() {
    recognizer?.stopListening()
  }

  override fun destroy() {
    recognizer?.destroy()
    recognizer = null
  }
}

// Public manager that coordinates listening and exposes a simple callback interface
class SpeechRecognizerManager(
  private val context: Context,
  private val listener: SpeechEventListener,
  private val engineFactory: (Context) -> SpeechEngine = { AndroidSpeechEngine(it) }
) {

  private var engine: SpeechEngine? = null
  private var currentLangs: List<Locale> = listOf(Locale.CHINA, Locale.US)

  private val internalListener = object : RecognitionListener {
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray) {}
    override fun onEndOfSpeech() {}
    override fun onEvent(eventType: Int, params: Bundle?) = false
    override fun onBeginningOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {
      val texts = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
      if (!texts.isNullOrEmpty()) listener.onPartial(texts[0])
    }
    override fun onResults(results: Bundle?) {
      val texts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
      if (!texts.isNullOrEmpty()) listener.onResult(texts[0])
    }
    override fun onError(error: Int) {
      listener.onError(error)
    }
  }

  fun startListening(langs: List<Locale> = currentLangs) {
    stopListening()
    currentLangs = langs
    engine = engineFactory(context).also { eng ->
      eng.setRecognitionListener(internalListener)
      val languageTag = localeLanguageTag(currentLangs.firstOrNull() ?: Locale.US)
      eng.startListening(languageTag)
    }
  }

  fun stopListening() {
    engine?.stopListening()
  }

  fun destroy() {
    engine?.destroy()
    engine = null
  }

  private fun localeLanguageTag(locale: Locale): String {
    val lang = locale.language
    val country = locale.country
    return if (country.isNotEmpty()) "${lang}_${country}" else lang
  }
}
