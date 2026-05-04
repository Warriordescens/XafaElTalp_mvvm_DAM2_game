package com.example.xafaeltalp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_history")
data class GameRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerName: String,
    val score: Int,
    val date: Long = System.currentTimeMillis(),
    val mode: String,
    val difficulty: String
)
