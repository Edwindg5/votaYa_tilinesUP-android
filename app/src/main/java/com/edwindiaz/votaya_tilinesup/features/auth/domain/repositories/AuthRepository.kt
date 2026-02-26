// features/auth/domain/repositories/AuthRepository.kt
package com.edwindiaz.votaya_tilinesup.features.auth.domain.repositories

import com.edwindiaz.votaya_tilinesup.features.auth.domain.entities.User

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User>
    fun signOut()
    fun getCurrentUser(): User?
    fun getCurrentUserPhotoUrl(): String?
}