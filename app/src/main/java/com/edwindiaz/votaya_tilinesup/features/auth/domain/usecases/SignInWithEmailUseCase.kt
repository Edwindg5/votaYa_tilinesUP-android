package com.edwindiaz.votaya_tilinesup.features.auth.domain.usecases

import com.edwindiaz.votaya_tilinesup.features.auth.domain.repositories.AuthRepository
import javax.inject.Inject

class SignInWithEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) =
        repository.signInWithEmail(email, password)
}
