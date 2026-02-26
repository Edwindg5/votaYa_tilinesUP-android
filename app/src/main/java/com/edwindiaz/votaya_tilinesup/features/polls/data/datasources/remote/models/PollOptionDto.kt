//PollOptionDto
package com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.remote.models

data class PollOptionDto(
    val id: String = "",
    val text: String = "",
    val votes: Int = 0
) {
    constructor() : this("", "", 0)
}