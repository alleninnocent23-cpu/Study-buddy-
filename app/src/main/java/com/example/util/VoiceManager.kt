package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

enum class VoiceGender {
    MALE,
    FEMALE
}

class VoiceManager(private val context: Context) {
    private val TAG = "VoiceManager"

    // Fish Audio TTS Service with user's API Key and Nigerian Voice Model
    private val fishAudioService = FishAudioTtsService(context)
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var fishJob: Job? = null

    // Voice Persona (Nigerian Male / Nigerian Female)
    private val _voiceGender = MutableStateFlow(VoiceGender.MALE)
    val voiceGender: StateFlow<VoiceGender> = _voiceGender.asStateFlow()

    // Text To Speech with Nigerian Accent
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private val nigerianLocale = Locale("en", "NG")

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Speech Recognizer
    private var speechRecognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    init {
        initTts()
    }

    fun setVoiceGender(gender: VoiceGender) {
        if (_voiceGender.value == gender) return
        _voiceGender.value = gender
        if (isSpeaking.value) {
            stopSpeaking()
        }
        applyVoiceSettings()
    }

    private fun applyVoiceSettings() {
        val currentTts = tts ?: return
        val isMale = _voiceGender.value == VoiceGender.MALE

        try {
            val voices = currentTts.voices
            if (!voices.isNullOrEmpty()) {
                val candidateVoice = if (isMale) {
                    voices.firstOrNull { v ->
                        (v.locale.country.equals("NG", ignoreCase = true) || v.name.lowercase().contains("nigeria") || v.name.lowercase().contains("en-ng")) &&
                        (v.name.lowercase().contains("male") || v.name.lowercase().contains("#m") || !v.name.lowercase().contains("female"))
                    } ?: voices.firstOrNull { v ->
                        v.locale.language == "en" && (v.name.lowercase().contains("male") || v.name.lowercase().contains("#m"))
                    }
                } else {
                    voices.firstOrNull { v ->
                        (v.locale.country.equals("NG", ignoreCase = true) || v.name.lowercase().contains("nigeria") || v.name.lowercase().contains("en-ng")) &&
                        (v.name.lowercase().contains("female") || v.name.lowercase().contains("#f"))
                    } ?: voices.firstOrNull { v ->
                        v.locale.language == "en" && v.name.lowercase().contains("female")
                    }
                }

                if (candidateVoice != null) {
                    currentTts.voice = candidateVoice
                    Log.i(TAG, "Selected voice for ${_voiceGender.value}: ${candidateVoice.name}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error choosing custom voice", e)
        }

        if (isMale) {
            // Natural, deep, resonant Nigerian male cadence (clears away thin robotic buzz)
            currentTts.setSpeechRate(0.93f)
            currentTts.setPitch(0.85f)
        } else {
            // Clear, articulate Nigerian female cadence
            currentTts.setSpeechRate(0.96f)
            currentTts.setPitch(1.04f)
        }
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Apply Nigerian accent: Locale("en", "NG")
                val langResult = tts?.setLanguage(nigerianLocale)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Locale en_NG not fully installed, falling back to British English for clear pronunciation")
                    try {
                        tts?.language = Locale.UK
                    } catch (e: Exception) {
                        tts?.language = Locale.getDefault()
                    }
                } else {
                    Log.i(TAG, "TTS initialized with Nigerian English locale: en_NG")
                }

                applyVoiceSettings()

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
                isTtsReady = true
                pendingSpeechText?.let { textToPlay ->
                    pendingSpeechText = null
                    speakWithLocalNigerianTts(textToPlay)
                }
            } else {
                Log.e(TAG, "Failed to initialize TTS engine")
            }
        }
    }

    private var pendingSpeechText: String? = null

    fun speak(rawText: String) {
        stopSpeaking()
        val cleanSpeechText = cleanTextForSpeech(rawText)
        val isMale = _voiceGender.value == VoiceGender.MALE

        // Try Fish Audio Nigerian voice model first using the user's API key
        fishJob = scope.launch {
            var fallbackTriggered = false
            val fishSuccess = fishAudioService.synthesizeAndPlay(
                text = cleanSpeechText,
                isMale = isMale,
                onStart = {
                    _isSpeaking.value = true
                },
                onCompletion = {
                    _isSpeaking.value = false
                },
                onError = { reason ->
                    Log.i(TAG, "Fish Audio fallback to Android Nigerian TTS: $reason")
                    fallbackTriggered = true
                    speakWithLocalNigerianTts(cleanSpeechText)
                }
            )

            if (!fishSuccess && !fallbackTriggered) {
                speakWithLocalNigerianTts(cleanSpeechText)
            }
        }
    }

    private fun speakWithLocalNigerianTts(cleanSpeechText: String) {
        if (!isTtsReady || tts == null) {
            pendingSpeechText = cleanSpeechText
            initTts()
            return
        }
        val utteranceId = "studybuddy_nigerian_${System.currentTimeMillis()}"

        val maxLen = 3900
        if (cleanSpeechText.length <= maxLen) {
            tts?.speak(cleanSpeechText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            val chunks = cleanSpeechText.chunked(maxLen)
            for (i in chunks.indices) {
                val mode = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                tts?.speak(chunks[i], mode, null, "${utteranceId}_$i")
            }
        }
        _isSpeaking.value = true
    }

    fun stopSpeaking() {
        fishJob?.cancel()
        fishJob = null
        fishAudioService.stop()
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
        _isSpeaking.value = false
    }

    fun startListening(onResult: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "Speech recognition not available on device")
            return
        }

        stopSpeaking()
        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        Log.w(TAG, "SpeechRecognizer error: $error")
                        _isListening.value = false
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val text = matches[0]
                            _recognizedText.value = text
                            onResult(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _recognizedText.value = matches[0]
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, nigerianLocale)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask StudyBuddy your school question...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognizer", e)
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recognizer", e)
        } finally {
            speechRecognizer = null
            _isListening.value = false
        }
    }

    fun shutdown() {
        stopSpeaking()
        stopListening()
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        } finally {
            tts = null
            isTtsReady = false
        }
    }

    private fun cleanTextForSpeech(text: String): String {
        return text
            .replace(Regex("\\?\\s*\\$\\$\\s*\\(\\s*\\)"), "")
            .replace(Regex("\\?\\s*\\$\\$"), "")
            .replace(Regex("\\$\\$\\s*\\(\\s*\\)"), "")
            .replace(Regex("\\$\\s*\\(\\s*\\)"), "")
            .replace("$$", "")
            .replace("$", "")
            .replace(Regex("[#*`_~>\\[\\]()]"), " ")
            .replace(Regex("\\b(Step \\d+:?)"), "$1. ")
            .replace("==", " equals ")
            .replace("!=", " does not equal ")
            .replace("<=", " is less than or equal to ")
            .replace(">=", " is greater than or equal to ")
            .replace("²", " squared")
            .replace("^2", " squared")
            .replace("³", " cubed")
            .replace("^3", " cubed")
            .replace("√", " square root of ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
