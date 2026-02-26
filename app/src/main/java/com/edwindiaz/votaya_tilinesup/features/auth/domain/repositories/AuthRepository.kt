//AuthRepository.kt
package com.edwindiaz.votaya_tilinesup.features.auth.domain.repositories

import com.edwindiaz.votaya_tilinesup.features.auth.domain.entities.User

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        username: String
    ): Result<User>
    suspend fun signOut()
    fun getCurrentUser(): User?
    fun getCurrentUserPhotoUrl(): String?
}