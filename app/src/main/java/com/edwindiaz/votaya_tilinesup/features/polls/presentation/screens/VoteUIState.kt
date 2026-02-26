package com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens

import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.Poll

data class VoteUIState(
    val isLoading: Boolean = false,
    val poll: Poll? = null,
    val selectedOptionId: String? = null,
    val isVoting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
