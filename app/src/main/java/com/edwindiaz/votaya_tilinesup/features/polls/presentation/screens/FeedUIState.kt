package com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens

import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.Poll

data class FeedUIState(
    val isLoading: Boolean = false,
    val polls: List<Poll> = emptyList(),
    val error: String? = null
)
