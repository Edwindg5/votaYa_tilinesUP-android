//com.edwindiaz.votaya_tilinesup/core/navigation/Screen.kt
package com.edwindiaz.votaya_tilinesup.core.navigation

import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object Register

@Serializable
object Feed

@Serializable
object CreatePoll

@Serializable
data class Vote(val pollId: String)

@Serializable
data class Results(val pollId: String)