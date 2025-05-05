package com.wordgame.util

import com.wordgame.data.db.entity.Word
import kotlin.random.Random

object GameUtils {
    
    /**
     * Calculates the potential maximum score for a game with the given words
     */
    fun calculateMaximumPossibleScore(words: List<Word>, timeRemaining: Int): Int {
        val wordPoints = words.sumOf { it.length * 100 }
        val timePoints = timeRemaining * 50
        return wordPoints + timePoints
    }
    
    /**
     * Generates a set of indices to reveal a certain number of letters from a word
     */
    fun getRandomIndicesToReveal(wordLength: Int, countToReveal: Int): Set<Int> {
        if (countToReveal >= wordLength) {
            return (0 until wordLength).toSet()
        }
        
        val indices = mutableSetOf<Int>()
        while (indices.size < countToReveal) {
            indices.add(Random.nextInt(wordLength))
        }
        
        return indices
    }
    
    /**
     * Calculates the percentage score
     */
    fun calculatePercentage(score: Int, maxPossibleScore: Int): Int {
        if (maxPossibleScore <= 0) return 0
        return ((score.toFloat() / maxPossibleScore) * 100).toInt()
    }
}
