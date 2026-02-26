// features/auth/data/repositories/AuthRepositoryImpl.kt
// features/auth/data/repositories/AuthRepositoryImpl.kt
package com.edwindiaz.votaya_tilinesup.features.auth.data.repositories

import android.util.Log
import com.edwindiaz.votaya_tilinesup.features.auth.data.datasources.remote.mapper.toDomain
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
        Log.d("AuthRepository", "🟡 Iniciando signInWithGoogle con token: ${idToken.take(10)}...")

        // Autenticar con Google
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val firebaseUser = result.user ?: throw Exception("Usuario no encontrado después de autenticación")

        Log.d("AuthRepository", "✅ Usuario autenticado en Firebase:")
        Log.d("AuthRepository", "   └─ UID: ${firebaseUser.uid}")
        Log.d("AuthRepository", "   └─ Email: ${firebaseUser.email}")
        Log.d("AuthRepository", "   └─ DisplayName: ${firebaseUser.displayName}")
        Log.d("AuthRepository", "   └─ PhotoUrl: ${firebaseUser.photoUrl}")

        // Verificar/Crear documento en Firestore
        val userRef = firestore.collection("users").document(firebaseUser.uid)
        Log.d("AuthRepository", "📂 Verificando documento en Firestore: users/${firebaseUser.uid}")

        val userDoc = userRef.get().await()

        val userDto = if (userDoc.exists()) {
            Log.d("AuthRepository", "📄 Usuario EXISTE en Firestore")
            Log.d("AuthRepository", "   └─ Datos actuales: ${userDoc.data}")
            userDoc.toObject(UserDto::class.java)!!
        } else {
            Log.d("AuthRepository", "➕ Usuario NO existe en Firestore - creando nuevo documento")

            // Crear nuevo usuario
            val newUser = UserDto(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName ?: "",
                username = firebaseUser.email?.substringBefore("@") ?: "usuario_${firebaseUser.uid.take(6)}",
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString()
            )

            Log.d("AuthRepository", "   └─ Datos a guardar:")
            Log.d("AuthRepository", "      ├─ uid: ${newUser.uid}")
            Log.d("AuthRepository", "      ├─ displayName: ${newUser.displayName}")
            Log.d("AuthRepository", "      ├─ username: ${newUser.username}")
            Log.d("AuthRepository", "      ├─ email: ${newUser.email}")
            Log.d("AuthRepository", "      └─ photoUrl: ${newUser.photoUrl}")

            // Guardar en Firestore
            userRef.set(newUser).await()
            Log.d("AuthRepository", "✅ Usuario guardado exitosamente en Firestore")

            // Verificar que se guardó correctamente
            val verifyDoc = userRef.get().await()
            if (verifyDoc.exists()) {
                Log.d("AuthRepository", "✅ Verificación: documento existe en Firestore")
            } else {
                Log.e("AuthRepository", "❌ ERROR: El documento no se guardó correctamente")
            }

            newUser
        }

        Log.d("AuthRepository", "🎉 Proceso de autenticación completado exitosamente")
        Result.success(userDto.toDomain())

    } catch (e: Exception) {
        Log.e("AuthRepository", "❌ Error en signInWithGoogle", e)
        Log.e("AuthRepository", "   └─ Mensaje: ${e.message}")
        Log.e("AuthRepository", "   └─ Causa: ${e.cause}")
        e.printStackTrace()
        Result.failure(e)
    }

    override fun signOut() {
        Log.d("AuthRepository", "🚪 Cerrando sesión")
        firebaseAuth.signOut()
        Log.d("AuthRepository", "✅ Sesión cerrada")
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: run {
            Log.d("AuthRepository", "👤 No hay usuario autenticado actualmente")
            return null
        }

        Log.d("AuthRepository", "👤 Usuario actual:")
        Log.d("AuthRepository", "   └─ UID: ${firebaseUser.uid}")
        Log.d("AuthRepository", "   └─ Email: ${firebaseUser.email}")

        return User(
            uid = firebaseUser.uid,
            displayName = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            photoUrl = firebaseUser.photoUrl?.toString()
        )
    }

    override fun getCurrentUserPhotoUrl(): String? {
        val photoUrl = firebaseAuth.currentUser?.photoUrl?.toString()
        Log.d("AuthRepository", "📸 PhotoUrl actual: $photoUrl")
        return photoUrl
    }

    /**
     * Función de utilidad para crear un usuario de prueba manualmente
     * Útil para desarrollo y pruebas
     */
    suspend fun createTestUser(uid: String = "pruebaUser"): Result<User> = try {
        Log.d("AuthRepository", "🧪 Creando usuario de prueba con UID: $uid")

        val testUser = UserDto(
            uid = uid,
            displayName = "Usuario Prueba",
            username = "prueba",
            email = "prueba@email.com",
            photoUrl = null
        )

        firestore.collection("users")
            .document(uid)
            .set(testUser)
            .await()

        Log.d("AuthRepository", "✅ Usuario de prueba creado exitosamente")
        Result.success(testUser.toDomain())

    } catch (e: Exception) {
        Log.e("AuthRepository", "❌ Error creando usuario de prueba", e)
        Result.failure(e)
    }
}