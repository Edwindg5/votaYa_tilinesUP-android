//com.edwindiaz.votaya_tilinesup/core/navigation/Screen.kt
package com.edwindiaz.votaya_tilinesup.core.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable data object Login : Screen()
    @Serializable data object Register : Screen()
    @Serializable data object Feed : Screen()
    @Serializable data object CreatePoll : Screen()
    @Serializable data class Vote(val pollId: String) : Screen()
    @Serializable data class Results(val pollId: String) : Screen()
}