// features/auth/presentation/viewmodels/LoginViewModel.kt
// features/auth/presentation/viewmodels/LoginViewModel.kt
package com.edwindiaz.votaya_tilinesup.features.auth.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwindiaz.votaya_tilinesup.features.auth.data.repositories.AuthRepositoryImpl
import com.edwindiaz.votaya_tilinesup.features.auth.domain.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            Log.d("LoginViewModel", "🔑 Iniciando login con Google")
            Log.d("LoginViewModel", "   └─ Token: ${idToken.take(20)}...")

            _uiState.update {
                it.copy(isLoading = true, error = null, isSuccess = false)
            }

            try {
                val result = authRepository.signInWithGoogle(idToken)

                result.fold(
                    onSuccess = { user ->
                        Log.d("LoginViewModel", "✅ Login exitoso para usuario: ${user.email}")
                        Log.d("LoginViewModel", "   └─ UID: ${user.uid}")
                        Log.d("LoginViewModel", "   └─ DisplayName: ${user.displayName}")

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = null,
                                isSuccess = true
                            )
                        }
                    },
                    onFailure = { exception ->
                        Log.e("LoginViewModel", "❌ Error en login", exception)
                        Log.e("LoginViewModel", "   └─ Mensaje: ${exception.message}")

                        val errorMessage = when {
                            exception.message?.contains("network") == true ->
                                "Error de red. Verifica tu conexión a internet."
                            exception.message?.contains("closed") == true ->
                                "La conexión se cerró. Intenta de nuevo."
                            exception.message?.contains("canceled") == true ->
                                "Inicio de sesión cancelado."
                            else ->
                                exception.message ?: "Error desconocido al iniciar sesión"
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = errorMessage,
                                isSuccess = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("LoginViewModel", "💥 Excepción no controlada en login", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}",
                        isSuccess = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetState() {
        _uiState.update { LoginUiState() }
    }
}