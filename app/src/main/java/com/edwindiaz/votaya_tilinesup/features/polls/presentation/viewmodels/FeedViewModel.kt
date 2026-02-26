package com.edwindiaz.votaya_tilinesup.features.polls.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwindiaz.votaya_tilinesup.features.polls.domain.usecases.ObservePollsUseCase
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens.FeedUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val observePolls: ObservePollsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUIState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init { loadPolls() }

    private fun loadPolls() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            observePolls()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    _events.emit(e.message ?: "Error al cargar encuestas")
                }
                .collect { polls ->
                    _uiState.update { it.copy(isLoading = false, polls = polls) }
                }
        }
    }
}
