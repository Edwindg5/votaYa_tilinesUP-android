package com.edwindiaz.votaya_tilinesup.features.polls.domain.entities

data class PollOption(
    val id: String = "",
    val text: String = "",
    val votes: Int = 0
)
