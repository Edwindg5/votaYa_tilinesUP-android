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

fun PollOption.toDto() = PollOptionDto(
    id = id,
    text = text,
    votes = votes
)

fun PollDto.toDomain(options: List<PollOption> = emptyList()): Poll {
    return Poll(
        id = id,
        question = title,  // Mapeamos title a question
        authorId = ownerId, // Mapeamos ownerId a authorId
        authorName = "",    // Esto lo obtendremos de otro lado
        options = options,
        totalVotes = totalVotes,
        createdAt = createdAt?.seconds?.times(1000) ?: 0L
    )
}
