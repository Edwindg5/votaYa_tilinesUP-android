//VoteViewModel.kt
package com.edwindiaz.votaya_tilinesup.features.polls.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwindiaz.votaya_tilinesup.features.polls.domain.usecases.GetPollByIdUseCase
import com.edwindiaz.votaya_tilinesup.features.polls.domain.usecases.VoteUseCase
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens.VoteUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoteViewModel @Inject constructor(
    private val getPollById: GetPollByIdUseCase,
    private val voteUseCase: VoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoteUIState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    fun loadPoll(pollId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getPollById(pollId).fold(
                onSuccess = { poll ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            poll = poll,
                            selectedOptionId = null,
                            isSuccess = false
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Error al cargar la encuesta"
                        )
                    }
                }
            )
        }
    }

    fun selectOption(optionId: String) {
        _uiState.update { it.copy(selectedOptionId = optionId) }
    }

    fun vote(pollId: String) {
        val optionId = _uiState.value.selectedOptionId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isVoting = true, error = null) }
            voteUseCase(pollId, optionId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isVoting = false,
                            isSuccess = true,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isVoting = false,
                            error = e.message ?: "Error al votar"
                        )
                    }
                    _events.emit(e.message ?: "Error al votar")
                }
            )
        }
    }

    fun resetState() {
        _uiState.update { VoteUIState() }
    }
}