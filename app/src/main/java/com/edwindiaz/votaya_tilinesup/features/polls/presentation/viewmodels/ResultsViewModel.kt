package com.edwindiaz.votaya_tilinesup.features.polls.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwindiaz.votaya_tilinesup.features.polls.domain.entities.Poll
import com.edwindiaz.votaya_tilinesup.features.polls.domain.usecases.ObservePollByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
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
    private val observePollById: ObservePollByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsUIState())
    val uiState = _uiState.asStateFlow()

    private var currentPollId: String? = null

    fun loadPoll(pollId: String) {
        // Si ya estamos observando el mismo pollId, no hacemos nada
        if (currentPollId == pollId) return

        currentPollId = pollId
        observePollInRealTime(pollId)
    }

    private fun observePollInRealTime(pollId: String) {
        viewModelScope.launch {
            observePollById(pollId)
                .onStart {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al cargar la encuesta"
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { poll ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    poll = poll,
                                    error = null
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
    }

    override fun onCleared() {
        super.onCleared()
        currentPollId = null
    }
}