package com.example.util

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.result.ActivityResultLauncher
import java.util.Locale

class SpeechManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.language = Locale.US
        }
    }

    fun speak(text: String, isMyanmar: Boolean = false) {
        if (!isInitialized) return
        val cleanText = text
            .replace(Regex("```[a-zA-Z]*\\n[\\s\\S]*?```"), "Code block omitted.")
            .replace(Regex("[*#_`]"), "")
            .take(500) // Don't speak overly long texts without limit

        if (isMyanmar) {
            // Attempt Myanmar or fallback to default
            val myLocale = Locale("my", "MM")
            val available = tts?.isLanguageAvailable(myLocale)
            if (available != null && available >= TextToSpeech.LANG_AVAILABLE) {
                tts?.language = myLocale
            }
        } else {
            tts?.language = Locale.US
        }

        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "NyintTTS_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking == true
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    companion object {
        fun launchSpeechRecognizer(
            launcher: ActivityResultLauncher<Intent>,
            languageCode: String = "en-US"
        ) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to A Nyint AI...")
            }
            try {
                launcher.launch(intent)
            } catch (e: Exception) {
                // Device might lack speech recognition activity
            }
        }
    }
}
