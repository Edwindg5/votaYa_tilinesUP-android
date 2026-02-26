//User.kt
package com.edwindiaz.votaya_tilinesup.features.auth.domain.entities

data class User(
    val uid: String = "",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String? = null
)