package com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "polls")
data class PollEntity(
    @PrimaryKey val id: String,
    val question: String,
    val authorId: String,
    val authorName: String,
    val optionsJson: String,
    val totalVotes: Int,
    val createdAt: Long,
    val status: String
)
