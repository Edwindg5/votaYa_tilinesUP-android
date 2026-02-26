// features/auth/presentation/screens/LoginScreen.kt
package com.edwindiaz.votaya_tilinesup.features.auth.presentation.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edwindiaz.votaya_tilinesup.R
import com.edwindiaz.votaya_tilinesup.features.auth.presentation.components.GoogleSignInButton
import com.edwindiaz.votaya_tilinesup.features.auth.presentation.viewmodels.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val webClientId = remember { context.getString(R.string.default_web_client_id) }

    Log.d("LoginScreen", "🔑 Web Client ID: $webClientId")

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    Log.d("LoginScreen", "✅ Google Sign-In exitoso: ${account.email}")
                    account.idToken?.let { idToken ->
                        loginViewModel.loginWithGoogle(idToken)
                    } ?: run {
                        scope.launch {
                            snackbarHostState.showSnackbar("Error: No se pudo obtener el token")
                        }
                    }
                } catch (e: ApiException) {
                    Log.e("LoginScreen", "❌ Error ${e.statusCode}: ${e.message}")
                    val errorMsg = when (e.statusCode) {
                        10 -> "Error de configuración: Client ID incorrecto"
                        12501 -> "Inicio de sesión cancelado"
                        else -> "Error ${e.statusCode}: ${e.message}"
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar(errorMsg)
                    }
                }
            }
        } else {
            Log.d("LoginScreen", "Login cancelado por el usuario")
        }
    }

    // Observar estado del login
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Log.d("LoginScreen", "✅ Login exitoso en ViewModel")
            onLoginSuccess()
            loginViewModel.resetState()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Log.e("LoginScreen", "❌ Error en ViewModel: $error")
            snackbarHostState.showSnackbar(error)
            loginViewModel.clearError()
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
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🗳️", fontSize = 64.sp)
                    Text(
                        text = "VotaYa",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    GoogleSignInButton(
                        onClick = {
                            scope.launch {
                                try {
                                    Log.d("LoginScreen", "🖱️ Click en botón de Google")
                                    // Cerrar sesión previa para forzar selección de cuenta
                                    googleSignInClient.signOut().await()
                                    launcher.launch(googleSignInClient.signInIntent)
                                } catch (e: Exception) {
                                    Log.e("LoginScreen", "❌ Error al lanzar Google Sign-In", e)
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                }
                            }
                        },
                        isLoading = false
                    )
                }
            }
        }
    }
}