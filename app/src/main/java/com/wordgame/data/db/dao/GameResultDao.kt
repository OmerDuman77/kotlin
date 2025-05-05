package com.wordgame.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wordgame.data.db.entity.GameResult
import kotlinx.coroutines.flow.Flow

@Dao
interface GameResultDao {
    @Query("SELECT * FROM game_results ORDER BY score DESC")
    fun getAllResults(): Flow<List<GameResult>>

    @Query("SELECT * FROM game_results ORDER BY score DESC LIMIT 10")
    fun getTopResults(): Flow<List<GameResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gameResult: GameResult): Long

    @Query("SELECT * FROM game_results WHERE id = :id")
    suspend fun getResultById(id: Int): GameResult?
}
