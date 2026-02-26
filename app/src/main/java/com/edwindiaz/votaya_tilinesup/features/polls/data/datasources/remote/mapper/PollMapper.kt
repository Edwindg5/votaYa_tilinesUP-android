// PollMapper.kt
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

fun PollOption.toDto() = PollOptionDto(
    id = id,
    text = text,
    votes = votes
)

fun PollDto.toDomain(options: List<PollOption> = emptyList(), authorName: String = ""): Poll {
    return Poll(
        id = id,
        question = title,
        authorId = ownerId,
        authorName = authorName,
        options = options,
        totalVotes = totalVotes,
        createdAt = createdAt
    )
}