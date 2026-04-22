package com.example.xafaeltalp.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User): Long
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUser(username: String): User?
}