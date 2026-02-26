//UserMapper.kt
package com.edwindiaz.votaya_tilinesup.features.auth.data.datasources.remote.mapper

import com.edwindiaz.votaya_tilinesup.features.auth.data.datasources.remote.models.UserDto
import com.edwindiaz.votaya_tilinesup.features.auth.domain.entities.User

fun UserDto.toDomain() = User(
    uid = uid,
    displayName = displayName,
    username = username,
    email = email,
    photoUrl = photoUrl
)

fun User.toDto() = UserDto(
    uid = uid,
    displayName = displayName,
    username = username,
    email = email,
    photoUrl = photoUrl
)
