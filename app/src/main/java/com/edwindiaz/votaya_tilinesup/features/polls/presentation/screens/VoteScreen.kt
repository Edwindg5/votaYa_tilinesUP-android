//VoteScreen.kt
package com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteScreen(
    pollId: String,
    onBack: () -> Unit,
    onVoteSuccess: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Votar") },
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
                text = "Encuesta ID: $pollId",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "¿Cuál es tu opción favorita?",
                style = MaterialTheme.typography.headlineSmall
            )

            // Opciones de ejemplo
            listOf("Opción A", "Opción B", "Opción C").forEach { option ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onVoteSuccess
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}