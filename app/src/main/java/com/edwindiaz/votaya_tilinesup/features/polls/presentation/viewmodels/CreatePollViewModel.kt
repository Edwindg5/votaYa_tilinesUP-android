//CreatePollViewModel.kt
package com.edwindiaz.votaya_tilinesup.features.polls.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwindiaz.votaya_tilinesup.features.polls.domain.usecases.CreatePollUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreatePollUIState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreatePollViewModel @Inject constructor(
    private val createPoll: CreatePollUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePollUIState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    fun createPoll(question: String, options: List<String>) {
        if (question.isBlank() || options.size < 2) {
            viewModelScope.launch { _events.emit("Agrega una pregunta y al menos 2 opciones") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            createPoll.invoke(question, options).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSuccess = true) } },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    _events.emit(e.message ?: "Error al crear encuesta")
                }
            )
        }
    }
}
