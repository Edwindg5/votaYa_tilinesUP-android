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
            _uiState.update { it.copy(isLoading = true) }
            getPollById(pollId).fold(
                onSuccess = { poll -> _uiState.update { it.copy(isLoading = false, poll = poll) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun selectOption(optionId: String) {
        _uiState.update { it.copy(selectedOptionId = optionId) }
    }

    fun vote(pollId: String) {
        val optionId = _uiState.value.selectedOptionId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isVoting = true) }
            voteUseCase(pollId, optionId).fold(
                onSuccess = { _uiState.update { it.copy(isVoting = false, isSuccess = true) } },
                onFailure = { e ->
                    _uiState.update { it.copy(isVoting = false, error = e.message) }
                    _events.emit(e.message ?: "Error al votar")
                }
            )
        }
    }
}
