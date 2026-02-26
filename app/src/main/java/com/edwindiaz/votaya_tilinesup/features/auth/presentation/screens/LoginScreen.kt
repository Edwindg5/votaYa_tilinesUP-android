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
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import android.util.Log

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

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                    val account = task.getResult(ApiException::class.java)
                    account.idToken?.let { idToken ->
                        Log.d("LoginScreen", "📨 Token obtenido, iniciando login...")
                        loginViewModel.loginWithGoogle(idToken)
                    }
                } catch (e: ApiException) {
                    Log.e("LoginScreen", "Error en Google Sign-In", e)
                    scope.launch {
                        snackbarHostState.showSnackbar("Error: ${e.message}")
                    }
                }
            }
        } else {
            Log.d("LoginScreen", "Login cancelado por el usuario")
        }
    }

    // Observar cuando el login es exitoso
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Log.d("LoginScreen", "✅ Login exitoso, notificando a AppNavigation")
            onLoginSuccess()
            loginViewModel.resetState()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
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
                            try {
                                Log.d("LoginScreen", "🖱️ Click en botón de Google")
                                launcher.launch(googleSignInClient.signInIntent)
                            } catch (e: Exception) {
                                Log.e("LoginScreen", "Error al lanzar Google Sign-In", e)
                                scope.launch {
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