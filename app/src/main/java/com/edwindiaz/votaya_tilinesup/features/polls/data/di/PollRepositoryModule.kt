//PollRepositoryModule.kt
package com.edwindiaz.votaya_tilinesup.features.polls.data.di

import com.edwindiaz.votaya_tilinesup.features.polls.data.repositories.PollRepositoryImpl
import com.edwindiaz.votaya_tilinesup.features.polls.domain.repositories.PollRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PollRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPollRepository(
        impl: PollRepositoryImpl
    ): PollRepository
}