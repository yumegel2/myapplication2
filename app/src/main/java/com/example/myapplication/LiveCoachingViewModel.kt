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

sealed class LiveCoachingUiState {
    object Idle : LiveCoachingUiState()
    object Listening : LiveCoachingUiState()
    data class Result(val transcript: String) : LiveCoachingUiState()
    data class Error(val message: String) : LiveCoachingUiState()
}

class LiveCoachingViewModel(app: Application) : AndroidViewModel(app) {
    private val _uiState = MutableStateFlow<LiveCoachingUiState>(LiveCoachingUiState.Idle)
    val uiState: StateFlow<LiveCoachingUiState> = _uiState.asStateFlow()

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
                _uiState.value = LiveCoachingUiState.Result(transcript)
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val transcript = matches?.firstOrNull() ?: ""
                _uiState.value = LiveCoachingUiState.Result(transcript)
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
}
