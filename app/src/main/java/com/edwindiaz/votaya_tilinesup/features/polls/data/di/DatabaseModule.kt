package com.edwindiaz.votaya_tilinesup.features.polls.data.di

import android.content.Context
import androidx.room.Room
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.local.dao.PollDao
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.local.db.PollsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePollsDatabase(@ApplicationContext context: Context): PollsDatabase =
        Room.databaseBuilder(context, PollsDatabase::class.java, "polls_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun providePollDao(db: PollsDatabase): PollDao = db.pollDao()
}
