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
        Log.d("LiveCoachingVM", "Starting generateResponse for: $transcript")
        
        // Check API key
        val apiKey = BuildConfig.GENAI_API_KEY
        if (apiKey.isBlank() || apiKey == "MISSING_GENAI_API_KEY") {
            Log.e("LiveCoachingVM", "API key is missing or invalid")
            return "(Error: API key is missing or invalid) Please add a valid Gemini API key to your local.properties file."
        }
        
        Log.d("LiveCoachingVM", "API Key length: ${apiKey.length}, First 5 chars: ${apiKey.take(5)}...")
        
        return try {
            // Create the model with explicit error handling
            val generativeModel = try {
                GenerativeModel(
                    // Use the correct model name format for the current API version
                    modelName = "models/gemini-1.5-pro",
                    apiKey = apiKey
                )
            } catch (e: Exception) {
                Log.e("LiveCoachingVM", "Error creating GenerativeModel", e)
                return "(Error creating AI model: ${e.message}) Please check your API key and internet connection."
            }
            
            // Create the prompt
            val prompt = "You are a supportive conversation coach. Give helpful, friendly, and actionable feedback to the following user's statement: \"$transcript\"."
            Log.d("LiveCoachingVM", "Using prompt: $prompt")
            
            // Generate content with explicit error handling
            val response = try {
                generativeModel.generateContent(prompt)
            } catch (e: Exception) {
                Log.e("LiveCoachingVM", "Error generating content", e)
                return "(Error generating content: ${e.message}) Please check your internet connection and try again."
            }
            
            // Extract text with explicit error handling
            val responseText = response.text
            if (responseText == null) {
                Log.e("LiveCoachingVM", "Response text is null")
                return "(Error: Empty response from AI) Please try again with a different prompt."
            }
            
            Log.d("LiveCoachingVM", "Successfully generated response: ${responseText.take(50)}...")
            responseText
            
        } catch (e: Exception) {
            Log.e("LiveCoachingVM", "Unexpected error in generateResponse", e)
            "(AI unavailable: ${e.javaClass.simpleName}: ${e.message}) Please try again later."
        }
    }

    fun submitReflection(reflection: String, scenario: String) {
        Log.d("LiveCoachingVM", "submitReflection called with: $reflection, scenario: $scenario")
        
        // Validate input
        if (reflection.isBlank()) {
            Log.e("LiveCoachingVM", "Reflection is blank, not submitting")
            return
        }
        
        viewModelScope.launch {
            try {
                // Set UI state to loading
                Log.d("LiveCoachingVM", "Setting UI state to Loading")
                _uiState.value = LiveCoachingUiState.Loading
                
                // Create prompt and generate response
                val prompt = "Scenario: $scenario. Reflection: $reflection"
                Log.d("LiveCoachingVM", "Generating response for prompt: $prompt")
                val response = generateResponse(prompt)
                Log.d("LiveCoachingVM", "Generated response: ${response.take(50)}...")
                
                // Create and add new result
                val newResult = CoachingResult(reflection, response)
                Log.d("LiveCoachingVM", "Creating new result: $newResult")
                
                // Get current results and add new one
                val currentResults = _results.value
                val updatedResults = currentResults + newResult
                
                // Update results state
                Log.d("LiveCoachingVM", "Updating results. Old size: ${currentResults.size}, New size: ${updatedResults.size}")
                _results.value = updatedResults
                
                // Verify update
                Log.d("LiveCoachingVM", "Results updated. Current size: ${_results.value.size}")
                if (_results.value.isNotEmpty()) {
                    Log.d("LiveCoachingVM", "Latest result: ${_results.value.last()}")
                }
                
                // Reset UI state
                _uiState.value = LiveCoachingUiState.Idle
                Log.d("LiveCoachingVM", "Reflection submission completed successfully")
                
            } catch (e: Exception) {
                Log.e("LiveCoachingVM", "Error in submitReflection", e)
                _uiState.value = LiveCoachingUiState.Error("Failed to process reflection: ${e.message}")
            }
        }
    }
}
