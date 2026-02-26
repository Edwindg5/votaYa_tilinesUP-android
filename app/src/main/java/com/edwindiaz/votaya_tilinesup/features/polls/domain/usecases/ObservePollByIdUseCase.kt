//ObservePollByIdUseCase.kt
package com.edwindiaz.votaya_tilinesup.features.polls.domain.usecases

import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.Poll
import com.edwindiaz.votaya_tilinesup.features.polls.domain.repositories.PollRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePollByIdUseCase @Inject constructor(
    private val repository: PollRepository
) {
    operator fun invoke(pollId: String): Flow<Result<Poll>> = repository.observePollById(pollId)
}