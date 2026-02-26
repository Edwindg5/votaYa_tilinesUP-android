//AuthRepositoryImpl.kt
package com.edwindiaz.votaya_tilinesup.features.auth.data.repositories

import android.util.Log
import com.edwindiaz.votaya_tilinesup.features.auth.data.datasources.remote.mapper.toDomain
import com.edwindiaz.votaya_tilinesup.features.auth.data.datasources.remote.mapper.toDto
import com.edwindiaz.votaya_tilinesup.features.auth.data.datasources.remote.models.UserDto
import com.edwindiaz.votaya_tilinesup.features.auth.domain.entities.User
import com.edwindiaz.votaya_tilinesup.features.auth.domain.repositories.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<User> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val firebaseUser = result.user!!

        // Verificar si el usuario ya existe en Firestore
        val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()

        val userDto = if (userDoc.exists()) {
            userDoc.toObject(UserDto::class.java)!!
        } else {
            // Crear nuevo usuario en Firestore
            UserDto(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName ?: "",
                username = firebaseUser.email?.substringBefore("@") ?: "",
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString()
            ).also {
                firestore.collection("users").document(firebaseUser.uid).set(it).await()
            }
        }

        Result.success(userDto.toDomain())
    } catch (e: Exception) {
        Log.e("AuthRepository", "Error en signInWithGoogle", e)
        Result.failure(e)
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> = try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user!!
        val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
        val userDto = doc.toObject(UserDto::class.java) ?: UserDto(
            uid = firebaseUser.uid,
            email = email,
            displayName = firebaseUser.displayName ?: "",
            photoUrl = firebaseUser.photoUrl?.toString()
        )
        Result.success(userDto.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        username: String
    ): Result<User> = try {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user!!
        val userDto = UserDto(
            uid = firebaseUser.uid,
            displayName = displayName,
            username = username,
            email = email,
            photoUrl = null
        )
        firestore.collection("users").document(firebaseUser.uid).set(userDto).await()
        Result.success(userDto.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signOut() {
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

    override fun getCurrentUserPhotoUrl(): String? {
        return firebaseAuth.currentUser?.photoUrl?.toString()
    }
}