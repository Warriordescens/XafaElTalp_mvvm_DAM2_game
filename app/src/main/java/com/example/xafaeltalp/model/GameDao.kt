package com.example.xafaeltalp.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GameDao {
    @Insert
    suspend fun insertRecord(record: GameRecord)

    @Query("SELECT * FROM game_history ORDER BY date DESC")
    suspend fun getAllRecords(): List<GameRecord>

    @Query("SELECT * FROM game_history ORDER BY score DESC LIMIT 10")
    suspend fun getTopScores(): List<GameRecord>
}
