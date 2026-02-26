package com.edwindiaz.votaya_tilinesup.features.polls.domain.usecases

import com.edwindiaz.votaya_tilinesup.features.polls.domain.repositories.PollRepository
import javax.inject.Inject

class VoteUseCase @Inject constructor(
    private val repository: PollRepository
) {
    suspend operator fun invoke(pollId: String, optionId: String) =
        repository.vote(pollId, optionId)
}
