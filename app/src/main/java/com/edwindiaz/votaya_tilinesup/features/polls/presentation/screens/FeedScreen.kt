//FeedScreen.kt
package com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.edwindiaz.votaya_tilinesup.features.auth.presentation.viewmodels.AuthViewModel
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.components.PollCard
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.viewmodels.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToCreatePoll: () -> Unit,
    onNavigateToVote: (String) -> Unit,
    onNavigateToResults: (String) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    feedViewModel: FeedViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val feedState by feedViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedState.error) {
        feedState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("VotaYa") },
                navigationIcon = {
                    if (authState.currentUser?.photoUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(authState.currentUser?.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                actions = {
                    Text(
                        text = authState.currentUser?.displayName?.split(" ")?.first() ?: "Usuario",
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreatePoll) {
                Icon(Icons.Default.Add, contentDescription = "Crear encuesta")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (feedState.isLoading && feedState.polls.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (feedState.polls.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No hay encuestas aún",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¡Crea la primera encuesta!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(feedState.polls) { poll ->
                        PollCard(
                            poll = poll,
                            onVoteClick = { onNavigateToVote(poll.id) },
                            onResultsClick = { onNavigateToResults(poll.id) }
                        )
                    }
                }
            }
        }
    }
}