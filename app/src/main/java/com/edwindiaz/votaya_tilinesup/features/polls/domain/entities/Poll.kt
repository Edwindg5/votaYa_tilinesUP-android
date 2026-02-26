package com.edwindiaz.votaya_tilinesup.features.polls.domain.entities

data class Poll(
    val id: String = "",
    val question: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val options: List<PollOption> = emptyList(),
    val totalVotes: Int = 0,
    val createdAt: Long = 0L,
    val status: PollStatus = PollStatus.PENDING
)

enum class PollStatus { PENDING, PUBLISHED, ERROR }
