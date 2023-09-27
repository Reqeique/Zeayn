package com.reqeique.zeayn.util

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Calendar
@Parcelize
@Entity(tableName = "thought_")
data class Thought(@PrimaryKey(autoGenerate = true)
                   val id: Int,val request: String, val dateTime:String= SimpleDateFormat("HH:mm dd/MM/yyyy").format(
    Calendar.getInstance().time)
    .toString()): Parcelable
sealed class Result()
class Success(val data: String): Result()
class Error(val error: Throwable): Result()