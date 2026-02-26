package com.edwindiaz.votaya_tilinesup.features.polls.domain.repositories

import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.Poll
import kotlinx.coroutines.flow.Flow

interface PollRepository {
    fun observePolls(): Flow<List<Poll>>
    suspend fun createPoll(question: String, options: List<String>): Result<Poll>
    suspend fun vote(pollId: String, optionId: String): Result<Unit>
    suspend fun getPollById(pollId: String): Result<Poll>
}
