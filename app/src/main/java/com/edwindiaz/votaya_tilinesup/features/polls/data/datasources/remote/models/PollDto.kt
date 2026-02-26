package com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.models

data class PollOptionDto(
    val id: String = "",
    val text: String = "",
    val votes: Int = 0
)

data class PollDto(
    val id: String = "",
    val question: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val options: Map<String, PollOptionDto> = emptyMap(),
    val totalVotes: Int = 0,
    val createdAt: Long = 0L
)
