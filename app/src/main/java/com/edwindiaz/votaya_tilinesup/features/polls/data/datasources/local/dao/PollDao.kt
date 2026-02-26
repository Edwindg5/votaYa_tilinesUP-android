package com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.local.dao

import androidx.room.*
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.local.entities.PollEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PollDao {
    @Query("SELECT * FROM polls ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PollEntity>>

    @Query("SELECT * FROM polls WHERE id = :pollId LIMIT 1")
    suspend fun getById(pollId: String): PollEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(poll: PollEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(polls: List<PollEntity>)

    @Delete
    suspend fun delete(poll: PollEntity)
}
