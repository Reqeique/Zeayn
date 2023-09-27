package com.reqeique.zeayn.util.databasae

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.reqeique.zeayn.util.Thought


@Database(entities = [Thought::class], version =1, exportSchema = false)

abstract class MainDatabase: RoomDatabase() {
    abstract fun thoughtDao(): ThoughtDao
    companion object {
        @Volatile
        private var INSTANCE: MainDatabase?  = null

        fun getDatabase(`this`: Context): MainDatabase {
            val temp = INSTANCE
            if (temp != null) {
                return temp
            }
            synchronized(this){

                val instance = Room.databaseBuilder(`this`, MainDatabase::class.java, "main_database").build()
                INSTANCE = instance
                return instance
            }


        }
    }
}