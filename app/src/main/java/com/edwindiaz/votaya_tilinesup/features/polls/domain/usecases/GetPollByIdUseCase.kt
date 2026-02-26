//GetPollByIdUseCase.kt
package com.edwindiaz.votaya_tilinesup.features.polls.domain.usecases

import com.edwindiaz.votaya_tilinesup.features.polls.domain.repositories.PollRepository
import javax.inject.Inject

class GetPollByIdUseCase @Inject constructor(
    private val repository: PollRepository
) {
    suspend operator fun invoke(pollId: String) =
        repository.getPollById(pollId)
}
