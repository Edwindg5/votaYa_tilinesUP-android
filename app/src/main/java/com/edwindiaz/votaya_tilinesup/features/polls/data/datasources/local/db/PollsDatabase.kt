package com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.local.dao.PollDao
import com.edwindiaz.votaya_tilinesup.features.polls.data.datasources.local.entities.PollEntity

@Database(entities = [PollEntity::class], version = 1, exportSchema = false)
abstract class PollsDatabase : RoomDatabase() {
    abstract fun pollDao(): PollDao
}
