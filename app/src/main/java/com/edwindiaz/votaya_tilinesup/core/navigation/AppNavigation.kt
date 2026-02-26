//AppNavigation.kt
package com.edwindiaz.votaya_tilinesup.core.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.edwindiaz.votaya_tilinesup.features.auth.presentation.screens.LoginScreen
import com.edwindiaz.votaya_tilinesup.features.auth.presentation.viewmodels.AuthViewModel
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens.CreatePollScreen
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens.FeedScreen
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens.ResultsScreen
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens.VoteScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var isSplashFinished by remember { mutableStateOf(false) }
    var navigationInProgress by remember { mutableStateOf(false) }

    Log.d("AppNavigation", "📱 Estado actual - isAuthenticated: ${authState.isAuthenticated}, isLoading: ${authState.isLoading}, isSplashFinished: $isSplashFinished")

    // Efecto para la pantalla de splash
    LaunchedEffect(Unit) {
        Log.d("AppNavigation", "⏳ Iniciando splash screen")
        delay(2000) // Mostrar splash por 2 segundos
        isSplashFinished = true
        Log.d("AppNavigation", "✅ Splash screen terminada")
    }

    // Efecto para navegación basada en autenticación
    LaunchedEffect(authState.isAuthenticated, authState.isLoading, isSplashFinished) {
        if (!authState.isLoading && isSplashFinished && !navigationInProgress) {
            navigationInProgress = true

            val currentRoute = navController.currentBackStackEntry?.destination?.route
            Log.d("AppNavigation", "🧭 Evaluando navegación - auth: ${authState.isAuthenticated}, route: $currentRoute")

            try {
                if (authState.isAuthenticated) {
                    if (currentRoute != "feed") {
                        Log.d("AppNavigation", "➡️ Navegando a FEED")
                        navController.navigate("feed") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    if (currentRoute != "login") {
                        Log.d("AppNavigation", "➡️ Navegando a LOGIN")
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            } finally {
                // Pequeño delay antes de permitir otra navegación
                delay(500)
                navigationInProgress = false
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    Log.d("AppNavigation", "🎉 Login exitoso, refrescando estado")
                    authViewModel.refreshAuthState()
                }
            )
        }

        composable("feed") {
            FeedScreen(
                onNavigateToCreatePoll = {
                    navController.navigate("create_poll")
                },
                onNavigateToVote = { pollId ->
                    navController.navigate("vote/$pollId")
                },
                onNavigateToResults = { pollId ->
                    navController.navigate("results/$pollId")
                },
                onSignOut = {
                    Log.d("AppNavigation", "🚪 Cerrando sesión desde Feed")
                    authViewModel.signOut()
                }
            )
        }

        composable("create_poll") {
            CreatePollScreen(
                onBack = { navController.popBackStack() },
                onPollCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "vote/{pollId}",
            arguments = listOf(navArgument("pollId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pollId = backStackEntry.arguments?.getString("pollId") ?: ""
            VoteScreen(
                pollId = pollId,
                onBack = { navController.popBackStack() },
                onVoteSuccess = {
                    navController.navigate("results/$pollId") {
                        popUpTo("vote/$pollId") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "results/{pollId}",
            arguments = listOf(navArgument("pollId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pollId = backStackEntry.arguments?.getString("pollId") ?: ""
            ResultsScreen(
                pollId = pollId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}