//Screen.kt
package com.edwindiaz.votaya_tilinesup.core.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
@Serializable
object Login

@OptIn(InternalSerializationApi::class)
@Serializable
object Feed

@OptIn(InternalSerializationApi::class)
@Serializable
object CreatePoll

@OptIn(InternalSerializationApi::class)
@Serializable
data class Vote(val pollId: String)

@OptIn(InternalSerializationApi::class)
@Serializable
data class Results(val pollId: String)