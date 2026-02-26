//FeedViewModel
package com.edwindiaz.votaya_tilinesup.features.polls.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwindiaz.votaya_tilinesup.features.polls.domain.usecases.ObservePollsUseCase
import com.edwindiaz.votaya_tilinesup.features.polls.presentation.screens.FeedUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        loadPolls()
    }

    fun loadPolls() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Pequeño delay para que se vea el indicador de carga
                delay(500)

                val polls = observePolls()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        polls = polls,
                        error = null  // Quitamos el error si no hay polls
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar encuestas"
                    )
                }
            }
        }
    }

    fun refreshPolls() {
        loadPolls()
    }
}