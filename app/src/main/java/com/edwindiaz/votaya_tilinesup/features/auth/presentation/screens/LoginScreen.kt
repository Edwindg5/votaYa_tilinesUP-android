//LoginScreen.kt
package com.edwindiaz.votaya_tilinesup.features.auth.presentation.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edwindiaz.votaya_tilinesup.core.auth.GoogleAuthUiClient
import com.edwindiaz.votaya_tilinesup.features.auth.presentation.components.GoogleSignInButton
import com.edwindiaz.votaya_tilinesup.features.auth.presentation.viewmodels.AuthViewModel
import com.edwindiaz.votaya_tilinesup.features.auth.presentation.viewmodels.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val googleAuthUiClient = remember { GoogleAuthUiClient(context, FirebaseAuth.getInstance()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                scope.launch {
                    googleAuthUiClient.signInWithIntent(intent).fold(
                        onSuccess = { idToken ->
                            loginViewModel.loginWithGoogle(idToken)
                        },
                        onFailure = { e ->
                            snackbarHostState.showSnackbar("Error: ${e.message}")
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            authViewModel.updateAuthState()
            onLoginSuccess()
        }
    }

    LaunchedEffect(Unit) {
        loginViewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🗳️",
                    fontSize = 64.sp
                )
                Text(
                    text = "VotaYa",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tu voz, en tiempo real",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Button(
                    onClick = { loginViewModel.loginWithEmail(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Iniciar sesión", fontWeight = FontWeight.SemiBold)
                    }
                }

                GoogleSignInButton(
                    onClick = {
                        try {
                            val signInIntent = googleAuthUiClient.getSignInIntent()
                            launcher.launch(signInIntent)
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al iniciar Google Sign In: ${e.message}")
                            }
                        }
                    },
                    isLoading = uiState.isLoading
                )

                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        "¿No tienes cuenta? Regístrate",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}