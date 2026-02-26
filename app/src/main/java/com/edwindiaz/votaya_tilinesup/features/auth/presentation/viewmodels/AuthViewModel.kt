// features/auth/presentation/viewmodels/AuthViewModel.kt
package com.edwindiaz.votaya_tilinesup.features.auth.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwindiaz.votaya_tilinesup.features.auth.domain.repositories.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CurrentUserInfo(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null
)

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true,
    val currentUserId: String? = null,
    val currentUser: CurrentUserInfo? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState(isLoading = true))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    init {
        Log.d("AuthViewModel", "🟡 Inicializando AuthViewModel")
        checkCurrentUser()
        observeAuthState()
    }

    private fun checkCurrentUser() {
        val currentUser = firebaseAuth.currentUser
        Log.d("AuthViewModel", "🔍 Verificando usuario actual: ${currentUser?.email ?: "null"}")

        _authState.update {
            it.copy(
                isAuthenticated = currentUser != null,
                isLoading = false,
                currentUserId = currentUser?.uid,
                currentUser = currentUser?.let { user ->
                    CurrentUserInfo(
                        uid = user.uid,
                        displayName = user.displayName ?: "",
                        email = user.email ?: "",
                        photoUrl = user.photoUrl?.toString()
                    )
                }
            )
        }
    }

    private fun observeAuthState() {
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user: FirebaseUser? = auth.currentUser
            Log.d("AuthViewModel", "🔄 Cambio en estado de autenticación: ${user?.email ?: "null"}")

            viewModelScope.launch {
                // Pequeño delay para asegurar que Firebase completa la operación
                delay(500)

                _authState.update { currentState ->
                    currentState.copy(
                        isAuthenticated = user != null,
                        isLoading = false,
                        currentUserId = user?.uid,
                        currentUser = user?.let {
                            CurrentUserInfo(
                                uid = it.uid,
                                displayName = it.displayName ?: "",
                                email = it.email ?: "",
                                photoUrl = it.photoUrl?.toString()
                            )
                        }
                    )
                }
                Log.d("AuthViewModel", "✅ Estado actualizado: isAuthenticated=${user != null}")
            }
        }
        firebaseAuth.addAuthStateListener(authStateListener!!)
        Log.d("AuthViewModel", "👂 Listener de autenticación registrado")
    }

    fun refreshAuthState() {
        Log.d("AuthViewModel", "🔄 Refrescando estado manualmente")
        checkCurrentUser()
    }

    fun signOut() {
        Log.d("AuthViewModel", "🚪 Cerrando sesión")
        viewModelScope.launch {
            authRepository.signOut()
            _authState.update {
                AuthState(isLoading = false, isAuthenticated = false)
            }
            Log.d("AuthViewModel", "✅ Sesión cerrada")
        }
    }

    override fun onCleared() {
        super.onCleared()
        authStateListener?.let {
            firebaseAuth.removeAuthStateListener(it)
            Log.d("AuthViewModel", "🗑️ Listener de autenticación removido")
        }
    }
}