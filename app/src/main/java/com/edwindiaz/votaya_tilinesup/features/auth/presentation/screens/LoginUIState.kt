package com.edwindiaz.votaya_tilinesup.features.auth.presentation.screens

data class LoginUIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
