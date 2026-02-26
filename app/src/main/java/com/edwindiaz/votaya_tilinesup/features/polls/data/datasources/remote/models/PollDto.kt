// PollDto.kt
package com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.models

data class PollDto(
    val id: String = "",
    val title: String = "",
    val ownerId: String = "",
    val totalVotes: Int = 0,
    val createdAt: Long = 0
) {
    // Constructor vacío necesario para Firebase
    constructor() : this("", "", "", 0, 0)
}