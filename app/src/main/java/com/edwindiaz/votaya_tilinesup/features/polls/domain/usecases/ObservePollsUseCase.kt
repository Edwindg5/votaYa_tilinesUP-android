package com.edwindiaz.votaya_tilinesup.features.polls.domain.usecases

import com.edwindiaz.votaya_tilinesup.features.polls.domain.repositories.PollRepository
import javax.inject.Inject

class ObservePollsUseCase @Inject constructor(
    private val repository: PollRepository
) {
    operator fun invoke() = repository.observePolls()
}
