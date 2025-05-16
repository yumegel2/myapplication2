package com.example.myapplication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.AccentGreen
import com.example.myapplication.ui.theme.SoftLavender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveCoachingScreen(viewModel: LiveCoachingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val results by viewModel.results.collectAsState()
    val apiKey = BuildConfig.GENAI_API_KEY
    var expanded by remember { mutableStateOf(false) }
    var selectedScenario by remember { mutableStateOf("Interview") }
    val scenarios = listOf("Interview", "First Date", "Team Meeting", "Negotiation", "Networking Event")
    val tips = mapOf(
        "Interview" to listOf(
            "Research the company and role beforehand.",
            "Practice common interview questions.",
            "Ask thoughtful questions at the end."
        ),
        "First Date" to listOf(
            "Be genuinely curious about your date.",
            "Share stories, not just facts.",
            "Keep the conversation light and positive."
        ),
        "Team Meeting" to listOf(
            "Listen before you speak.",
            "Summarize key points to show understanding.",
            "Encourage quieter team members to share."
        ),
        "Negotiation" to listOf(
            "Know your goals and limits.",
            "Listen for the other side's needs.",
            "Aim for a win-win outcome."
        ),
        "Networking Event" to listOf(
            "Prepare a short, friendly introduction.",
            "Ask open-ended questions.",
            "Follow up after the event."
        )
    )

    var reflection by remember { mutableStateOf("") }
    var showTips by remember { mutableStateOf(false) }
    var showApiWarning by remember { mutableStateOf(false) }
    var lastSubmitted by remember { mutableStateOf("") }
    var lastResponse by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    fun onEnterReflection() {
        if (apiKey.isBlank() || apiKey == "MISSING_GENAI_API_KEY") {
            showApiWarning = true
        } else {
            showApiWarning = false
            lastSubmitted = reflection
            android.util.Log.d("LiveCoachingScreen", "Submitting reflection: $reflection")
            viewModel.submitReflection(reflection, selectedScenario)
            reflection = ""
        }
    }
    
    // Update lastResponse whenever results change
    LaunchedEffect(key1 = results.size) {
        android.util.Log.d("LiveCoachingScreen", "LaunchedEffect triggered, results size: ${results.size}")
        if (results.isNotEmpty()) {
            val latestResponse = results.last().response
            android.util.Log.d("LiveCoachingScreen", "Setting lastResponse to: ${latestResponse.take(50)}...")
            lastResponse = latestResponse
        } else {
            android.util.Log.d("LiveCoachingScreen", "Results list is empty")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Live Coaching",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scenario selector
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Scenario: $selectedScenario",
                                style = MaterialTheme.typography.titleMedium,
                                color = PrimaryBlue
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = if (expanded) "Collapse" else "Expand",
                                tint = PrimaryBlue
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = expanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                scenarios.forEach { scenario ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedScenario = scenario
                                                expanded = false
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedScenario == scenario,
                                            onClick = {
                                                selectedScenario = scenario
                                                expanded = false
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = PrimaryBlue
                                            )
                                        )
                                        Text(
                                            scenario,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Tips section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SoftLavender.copy(alpha = 0.2f)
                    ),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTips = !showTips },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Lightbulb,
                                    contentDescription = "Tips",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    "Coaching Tips",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(start = 8.dp),
                                    color = PrimaryBlue
                                )
                            }
                            Icon(
                                imageVector = if (showTips) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = if (showTips) "Hide tips" else "Show tips",
                                tint = PrimaryBlue
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = showTips,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                tips[selectedScenario]?.forEach { tip ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .padding(top = 8.dp)
                                                .background(AccentGreen, CircleShape)
                                        )
                                        Text(
                                            tip,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Reflection input
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Reflection",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Reflection Prompt",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 8.dp),
                                color = PrimaryBlue
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = reflection,
                            onValueChange = { reflection = it },
                            label = { Text("What is one thing you want to try in your next ${selectedScenario.lowercase()}?") },
                            singleLine = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                cursorColor = PrimaryBlue
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Submit button
                        Button(
                            onClick = { onEnterReflection() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = reflection.isNotBlank() && uiState !is LiveCoachingUiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState is LiveCoachingUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        "Generating feedback...",
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Send,
                                        contentDescription = "Submit",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "Submit Reflection",
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Status messages
                if (showApiWarning) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            "API key missing! Please set your GENAI_API_KEY in your environment for full functionality.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                if (lastSubmitted.isNotBlank() && !showApiWarning) {
                    // Display the latest response directly in this screen
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PrimaryBlue.copy(alpha = 0.1f)
                        ),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Your Reflection:",
                                style = MaterialTheme.typography.labelLarge,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                lastSubmitted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                "Coach's Feedback:",
                                style = MaterialTheme.typography.labelLarge,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            if (uiState is LiveCoachingUiState.Loading) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = PrimaryBlue,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        "Generating feedback...",
                                        modifier = Modifier.padding(start = 8.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            } else if (lastResponse.isNotEmpty()) {
                                Text(
                                    lastResponse,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else if (results.isNotEmpty()) {
                                Text(
                                    results.last().response,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text(
                                    "Waiting for response...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    
                    // Debug information card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Debug Information",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text("UI State: ${uiState.javaClass.simpleName}")
                            Text("Results Size: ${results.size}")
                            Text("Last Response Set: ${lastResponse.isNotEmpty()}")
                            Text("API Key Present: ${apiKey.isNotBlank() && apiKey != "MISSING_GENAI_API_KEY"}")
                            
                            if (results.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Latest Result:")
                                Text("- Transcript: ${results.last().transcript.take(20)}...")
                                Text("- Response: ${results.last().response.take(20)}...")
                            }
                            
                            if (uiState is LiveCoachingUiState.Error) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Error: ${(uiState as LiveCoachingUiState.Error).message}",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview
@Composable
fun LiveCoachingScreenPreview() {
    LiveCoachingScreen()
}
