//AuthViewModel.kt
package com.edwindiaz.votaya_tilinesup.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwindiaz.votaya_tilinesup.features.auth.domain.entities.User
import com.edwindiaz.votaya_tilinesup.features.auth.domain.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState(
        isAuthenticated = authRepository.getCurrentUser() != null,
        currentUser = authRepository.getCurrentUser()
    ))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun updateAuthState() {
        _authState.update {
            it.copy(
                isAuthenticated = authRepository.getCurrentUser() != null,
                currentUser = authRepository.getCurrentUser()
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.update {
                AuthState(isAuthenticated = false, currentUser = null)
            }
        }
    }
}