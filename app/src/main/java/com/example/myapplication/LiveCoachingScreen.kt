package com.example.myapplication


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveCoachingScreen() {
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
    var apiKey by remember { mutableStateOf(System.getenv("GENAI_API_KEY") ?: "") }
    var showApiWarning by remember { mutableStateOf(false) }
    var lastSubmitted by remember { mutableStateOf("") }

    fun onEnterReflection() {
        if (apiKey.isBlank()) {
            showApiWarning = true
        } else {
            // TODO: Use the API key to process the reflection (e.g., send to backend or AI service)
            showApiWarning = false
            lastSubmitted = reflection
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Live Coaching", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.clickable { expanded = true }
            ) {
                Text("Scenario: $selectedScenario", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                scenarios.forEach { scenario ->
                    DropdownMenuItem(text = { Text(scenario) }, onClick = {
                        selectedScenario = scenario
                        expanded = false
                        showTips = false
                    })
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.clickable { showTips = !showTips }
            ) {
                Text(
                    if (showTips) "Hide Tips" else "Show Tips",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (showTips) {
                tips[selectedScenario]?.forEach { tip ->
                    Text("• $tip", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Reflection Prompt:", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = reflection,
                onValueChange = { reflection = it },
                label = { Text("What is one thing you want to try in your next ${selectedScenario.lowercase()}?") },
                singleLine = false,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.Button(onClick = { onEnterReflection() }) {
                Text("Enter")
            }
            if (showApiWarning) {
                Text(
                    "API key missing! Please set your GENAI_API_KEY in your environment for full functionality.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (lastSubmitted.isNotBlank() && !showApiWarning) {
                Text(
                    "Reflection submitted!",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}





@Preview
@Composable
fun LiveCoachingScreenPreview() {
    LiveCoachingScreen()
}
