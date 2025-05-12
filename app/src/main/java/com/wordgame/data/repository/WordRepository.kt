package com.wordgame.data.repository

import android.util.Log
import com.wordgame.data.WordData
import com.wordgame.data.db.dao.WordDao
import com.wordgame.data.db.entity.Word
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WordRepository @Inject constructor(
    private val wordDao: WordDao
) {
    fun getAllWords(): Flow<List<Word>> = wordDao.getAllWords()

    suspend fun addStaticWords() {
        if (wordDao.getAnyWord() == null) {
            wordDao.insertAll(WordData.wordList)
        }

    }

    fun getWordsByLength(length: Int): Flow<List<Word>> = wordDao.getWordsByLength(length)

    suspend fun getWordById(id: Int): Word? = wordDao.getWordById(id)

    suspend fun insert(word: Word) = wordDao.insert(word)

    suspend fun insertAll(words: List<Word>) = wordDao.insertAll(words)

    suspend fun getRandomWordByLength(length: Int): Word? = wordDao.getRandomWordByLength(length)

    suspend fun getWordsForGame(): List<Word> {
        val gameWords = mutableListOf<Word>()
        for (length in 3..10) {
            Log.d("WordRepo", "Getting random word for length $length")
            val word = wordDao.getRandomWordByLength(length)
            Log.d("WordRepo", "Got word '$word' for length $length")
            word?.let {
                gameWords.add(it)
            }
        }
        Log.d("WordRepo", "getWordsForGame() returning list of size ${gameWords.size}")
        return gameWords
    }
}
