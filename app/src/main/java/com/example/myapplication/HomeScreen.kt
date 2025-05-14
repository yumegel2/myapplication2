package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen(onStartCoaching: () -> Unit, onViewResults: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to EchoPal!", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onStartCoaching) {
                Text("Start Live Coaching")
            }
            Button(onClick = onViewResults) {
                Text("View Results")
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen({}, {})
}
