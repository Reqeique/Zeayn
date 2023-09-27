package com.reqeique.zeayn.util.databasae

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.reqeique.zeayn.util.Thought
import kotlinx.coroutines.flow.Flow

@Dao
interface ThoughtDao {
    @Query("SELECT * FROM thought_ ")
    fun getAllThought(): Flow<List<Thought>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addThought(thought: Thought): Long

    @Delete
    suspend fun deleteThought(note: Thought)

}