//FeedViewModel
package com.edwindiaz.votaya_tilinesup.features.polls.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwindiaz.votaya_tilinesup.features.polls.domain.usecases.ObservePollsUseCase
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens.FeedUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val observePolls: ObservePollsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUIState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        observePollsInRealTime()
    }

    private fun observePollsInRealTime() {
        viewModelScope.launch {
            observePolls()
                .onStart {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al cargar encuestas"
                        )
                    }
                }
                .collect { polls ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            polls = polls,
                            error = null
                        )
                    }
                }
        }
    }

    fun refreshPolls() {
        // No necesitamos hacer nada, el Flow ya está observando
        // Pero podemos forzar una recarga si es necesario
        observePollsInRealTime()
    }
}