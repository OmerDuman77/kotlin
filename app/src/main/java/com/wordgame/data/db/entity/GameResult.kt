package com.wordgame.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "game_results")
data class GameResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val score: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val timeRemaining: Int,
    val date: Long = System.currentTimeMillis()
)
