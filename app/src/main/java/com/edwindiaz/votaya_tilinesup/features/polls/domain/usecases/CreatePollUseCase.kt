package com.edwindiaz.votaya_tilinesup.features.polls.domain.usecases

import com.edwindiaz.votaya_tilinesup.features.polls.domain.repositories.PollRepository
import javax.inject.Inject

class CreatePollUseCase @Inject constructor(
    private val repository: PollRepository
) {
    suspend operator fun invoke(question: String, options: List<String>) =
        repository.createPoll(question, options)
}
