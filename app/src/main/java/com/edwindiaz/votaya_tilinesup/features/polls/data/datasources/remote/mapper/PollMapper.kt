//PollMapper.kt
package com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.mapper

import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.models.PollDto
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.models.PollOptionDto
import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.Poll
import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.PollOption

fun PollOptionDto.toDomain() = PollOption(
    id = id,
    text = text,
    votes = votes
)

fun PollDto.toDomain(): Poll {
    val optionsList = options.values.map { it.toDomain() }

    return Poll(
        id = id,
        question = question,
        authorId = authorId,
        authorName = authorName,
        options = optionsList,
        totalVotes = totalVotes,
        createdAt = createdAt
    )
}
