package com.edwindiaz.votaya_tilinesup.features.auth.presentation.screens

data class RegisterUIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
