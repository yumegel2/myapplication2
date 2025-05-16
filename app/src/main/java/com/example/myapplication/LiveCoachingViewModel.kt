package com.example.myapplication

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.ai.client.generativeai.GenerativeModel
import com.example.myapplication.BuildConfig
import android.util.Log

sealed class LiveCoachingUiState {
    object Idle : LiveCoachingUiState()
    object Listening : LiveCoachingUiState()
    object Loading : LiveCoachingUiState()
    data class Error(val message: String) : LiveCoachingUiState()
}

data class CoachingResult(val transcript: String, val response: String)

class LiveCoachingViewModel(app: Application) : AndroidViewModel(app) {
    private val _uiState = MutableStateFlow<LiveCoachingUiState>(LiveCoachingUiState.Idle)
    val uiState: StateFlow<LiveCoachingUiState> = _uiState.asStateFlow()

    private val _results = MutableStateFlow<List<CoachingResult>>(emptyList())
    val results: StateFlow<List<CoachingResult>> = _results.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening() {
        val context = getApplication<Application>()
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _uiState.value = LiveCoachingUiState.Listening
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                _uiState.value = LiveCoachingUiState.Error("Speech recognition error: $error")
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val transcript = matches?.firstOrNull() ?: ""
                if (transcript.isNotBlank()) {
                    viewModelScope.launch {
                        val response = generateResponse(transcript)
                        val newResult = CoachingResult(transcript, response)
                        _results.value = _results.value + newResult
                    }
                }
                _uiState.value = LiveCoachingUiState.Idle
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val transcript = matches?.firstOrNull() ?: ""
                // Optionally handle partial results, but don't save until final
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _uiState.value = LiveCoachingUiState.Idle
    }

    override fun onCleared() {
        speechRecognizer?.destroy()
        super.onCleared()
    }

    private suspend fun generateResponse(transcript: String): String {
        return try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-pro",
                apiKey = BuildConfig.GENAI_API_KEY
            )
            val prompt = "You are a supportive conversation coach. Give helpful, friendly, and actionable feedback to the following user's statement: \"$transcript\"."
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Great job! Keep practicing."
        } catch (e: Exception) {
            "(AI unavailable) Good job! Keep practicing."
        }
    }

    fun submitReflection(reflection: String, scenario: String) {
        Log.d("LiveCoachingVM", "submitReflection called with: $reflection, scenario: $scenario")
        viewModelScope.launch {
            _uiState.value = LiveCoachingUiState.Loading
            val prompt = "Scenario: $scenario. Reflection: $reflection"
            val response = generateResponse(prompt)
            val newResult = CoachingResult(reflection, response)
            Log.d("LiveCoachingVM", "Adding result: $newResult")
            _results.value = _results.value + newResult
            Log.d("LiveCoachingVM", "Results list now: ${_results.value}")
            _uiState.value = LiveCoachingUiState.Idle
        }
    }
}
