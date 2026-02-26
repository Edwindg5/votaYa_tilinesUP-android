//SignInWithGoogleUseCase.kt
package com.edwindiaz.votaya_tilinesup.features.auth.domain.usecases

import com.edwindiaz.votaya_tilinesup.features.auth.domain.repositories.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(idToken: String) =
        repository.signInWithGoogle(idToken)
}
