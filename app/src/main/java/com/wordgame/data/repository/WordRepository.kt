package com.wordgame.data.repository

import com.wordgame.data.db.dao.WordDao
import com.wordgame.data.db.entity.Word
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WordRepository @Inject constructor(
    private val wordDao: WordDao
) {
    fun getAllWords(): Flow<List<Word>> = wordDao.getAllWords()

    fun getWordsByLength(length: Int): Flow<List<Word>> = wordDao.getWordsByLength(length)

    suspend fun getWordById(id: Int): Word? = wordDao.getWordById(id)

    suspend fun insert(word: Word) = wordDao.insert(word)

    suspend fun insertAll(words: List<Word>) = wordDao.insertAll(words)

    suspend fun getRandomWordByLength(length: Int): Word? = wordDao.getRandomWordByLength(length)

    // Get a word for each required length (3-10)
    suspend fun getWordsForGame(): List<Word> {
        val gameWords = mutableListOf<Word>()
        for (length in 3..10) {
            wordDao.getRandomWordByLength(length)?.let {
                gameWords.add(it)
            }
        }
        return gameWords
    }
}
