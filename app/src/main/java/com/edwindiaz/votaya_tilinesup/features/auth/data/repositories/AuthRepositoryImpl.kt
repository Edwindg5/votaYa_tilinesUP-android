// features/auth/data/repositories/AuthRepositoryImpl.kt
package com.edwindiaz.votaya_tilinesup.features.auth.data.repositories

import android.util.Log
import com.edwindiaz.votaya_tilinesup.features.auth.domain.entities.User
import com.edwindiaz.votaya_tilinesup.features.auth.domain.repositories.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val database: FirebaseDatabase
) : AuthRepository {

    private val usersRef = database.getReference("users")

    override suspend fun signInWithGoogle(idToken: String): Result<User> = try {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val firebaseUser = result.user ?: throw Exception("Usuario no encontrado")

        val userRef = usersRef.child(firebaseUser.uid)
        val snapshot = userRef.get().await()

        if (!snapshot.exists()) {
            val newUser = mapOf(
                "uid" to firebaseUser.uid,
                "displayName" to (firebaseUser.displayName ?: ""),
                "username" to (firebaseUser.email?.substringBefore("@") ?: "user"),
                "email" to (firebaseUser.email ?: ""),
                "photoUrl" to firebaseUser.photoUrl?.toString()
            )

            userRef.setValue(newUser).await()
        }

        Result.success(
            User(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString()
            )
        )

    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null

        return User(
            uid = firebaseUser.uid,
            displayName = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            photoUrl = firebaseUser.photoUrl?.toString()
        )
    }

    override fun getCurrentUserPhotoUrl(): String? =
        firebaseAuth.currentUser?.photoUrl?.toString()
}