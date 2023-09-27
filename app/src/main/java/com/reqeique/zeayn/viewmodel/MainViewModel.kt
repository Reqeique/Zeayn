package com.reqeique.zeayn.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reqeique.zeayn.util.Thought
import com.reqeique.zeayn.util.databasae.MainDatabase
import com.reqeique.zeayn.util.databasae.ThoughtRepo
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val thoughtRepo: ThoughtRepo
    init {

        thoughtRepo = ThoughtRepo( MainDatabase.getDatabase(app).thoughtDao())

    }
    @Composable
    fun getAllThought(): State<List<Thought>>{
//        state
       return thoughtRepo.getAllThought.collectAsState(listOf())
    }
    suspend fun addThought(thought: Thought){
        thoughtRepo.addThought(thought)
    }
    suspend fun deleteThought(thought: Thought){
        thoughtRepo.deleteThought(thought)
    }


}