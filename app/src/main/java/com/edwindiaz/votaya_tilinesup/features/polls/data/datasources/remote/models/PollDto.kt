//PollDto.kt
package com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.models

import com.google.firebase.Timestamp

data class PollDto(
    val id: String = "",
    val title: String = "",
    val ownerId: String = "",
    val totalVotes: Int = 0,
    val createdAt: Timestamp? = null
)