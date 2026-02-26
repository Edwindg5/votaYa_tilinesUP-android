//ResultsScreen.kt
package com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    pollId: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Resultados de la encuesta",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "ID: $pollId",
                style = MaterialTheme.typography.bodyLarge
            )

            // Resultados de ejemplo
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Opción A: 45%")
                    LinearProgressIndicator(progress = 0.45f)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Opción B: 30%")
                    LinearProgressIndicator(progress = 0.3f)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Opción C: 25%")
                    LinearProgressIndicator(progress = 0.25f)
                }
            }
        }
    }
}