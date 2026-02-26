package com.edwindiaz.votaya_tilinesup.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.edwindiaz.votaya_tilinesup.features.auth.presentation.screens.LoginScreen
import com.edwindiaz.votaya_tilinesup.features.auth.presentation.screens.RegisterScreen
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens.CreatePollScreen
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens.FeedScreen
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens.ResultsScreen
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens.VoteScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("feed") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate("feed") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("feed") {
            FeedScreen(
                onNavigateToCreatePoll = { navController.navigate("create_poll") },
                onNavigateToVote = { pollId -> navController.navigate("vote/$pollId") },
                onNavigateToResults = { pollId -> navController.navigate("results/$pollId") }
            )
        }

        composable("create_poll") {
            CreatePollScreen(
                onBack = { navController.popBackStack() },
                onPollCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = "vote/{pollId}",
            arguments = listOf(navArgument("pollId") { type = NavType.StringType })
        ) { backStack ->
            val pollId = backStack.arguments?.getString("pollId") ?: ""
            VoteScreen(
                pollId = pollId,
                onBack = { navController.popBackStack() },
                onVoteSuccess = { navController.navigate("results/$pollId") }
            )
        }

        composable(
            route = "results/{pollId}",
            arguments = listOf(navArgument("pollId") { type = NavType.StringType })
        ) { backStack ->
            val pollId = backStack.arguments?.getString("pollId") ?: ""
            ResultsScreen(
                pollId = pollId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}