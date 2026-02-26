// features/auth/presentation/viewmodels/LoginViewModel.kt
package com.edwindiaz.votaya_tilinesup.features.auth.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            Log.d("LoginViewModel", "Iniciando login con Google, token length: ${idToken.length}")

            val result = authRepository.signInWithGoogle(idToken)

            result.onSuccess { user ->
                Log.d("LoginViewModel", "✅ Login exitoso: ${user.displayName}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                }
            }.onFailure { error ->
                Log.e("LoginViewModel", "❌ Login falló: ${error.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = error.message ?: "Error desconocido"
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { LoginUiState() }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}