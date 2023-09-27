package com.reqeique.zeayn.util.databasae

import com.reqeique.zeayn.util.Thought
import kotlinx.coroutines.flow.Flow

open class ThoughtRepo(private val thoughtDao: ThoughtDao) {
    suspend fun addThought(thought: Thought ): Long = thoughtDao.addThought(thought)
    val getAllThought : Flow<List<Thought>> = thoughtDao.getAllThought()
    suspend fun deleteThought(thought: Thought): Unit = thoughtDao.deleteThought(thought)


}