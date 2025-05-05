package com.wordgame.data.repository

import com.wordgame.data.db.dao.GameResultDao
import com.wordgame.data.db.entity.GameResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GameRepository @Inject constructor(
    private val gameResultDao: GameResultDao
) {
    fun getAllResults(): Flow<List<GameResult>> = gameResultDao.getAllResults()

    fun getTopResults(): Flow<List<GameResult>> = gameResultDao.getTopResults()

    suspend fun saveResult(gameResult: GameResult): Long = gameResultDao.insert(gameResult)

    suspend fun getResultById(id: Int): GameResult? = gameResultDao.getResultById(id)
}
