package com.edwindiaz.votaya_tilinesup.features.polls.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.Poll
import com.edwindiaz.votaya_tilinesup.features.polls.domain.usecases.GetPollByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultsUIState(
    val isLoading: Boolean = false,
    val poll: Poll? = null,
    val error: String? = null
)

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val getPollById: GetPollByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsUIState())
    val uiState = _uiState.asStateFlow()

    fun loadPoll(pollId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getPollById(pollId).fold(
                onSuccess = { poll ->
                    _uiState.update { it.copy(isLoading = false, poll = poll) }
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
}