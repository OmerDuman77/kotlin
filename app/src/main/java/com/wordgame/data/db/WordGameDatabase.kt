package com.wordgame.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wordgame.data.db.dao.GameResultDao
import com.wordgame.data.db.dao.WordDao
import com.wordgame.data.db.entity.GameResult
import com.wordgame.data.db.entity.Word
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Word::class, GameResult::class], version = 1, exportSchema = false)
abstract class WordGameDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun gameResultDao(): GameResultDao

    companion object {
        @Volatile
        private var INSTANCE: WordGameDatabase? = null

        fun getInstance(context: Context): WordGameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordGameDatabase::class.java,
                    "word_game_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                prePopulateDatabase(database.wordDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prePopulateDatabase(wordDao: WordDao) {
            val wordsList = listOf(
                Word(0, "cat", "A small domesticated carnivorous mammal", 3),
                Word(0, "dog", "A domesticated carnivorous mammal", 3),
                Word(0, "sun", "The star around which the earth orbits", 3),
                Word(0, "bike", "A two-wheeled vehicle", 4),
                Word(0, "book", "A written or printed work", 4),
                Word(0, "tree", "A woody perennial plant", 4),
                Word(0, "river", "A large natural stream of water", 5),
                Word(0, "piano", "A musical instrument", 5),
                Word(0, "stone", "Hard solid nonmetallic mineral matter", 5),
                Word(0, "garden", "A piece of ground for growing plants", 6),
                Word(0, "monkey", "A primate mammal", 6),
                Word(0, "window", "An opening in a wall or roof", 6),
                Word(0, "rainbow", "An arch of colors in the sky", 7),
                Word(0, "diamond", "A precious gemstone", 7),
                Word(0, "freedom", "The power to act or speak without restraint", 7),
                Word(0, "elephant", "A very large plant-eating mammal", 8),
                Word(0, "calendar", "A chart showing the days of a year", 8),
                Word(0, "mountain", "A large natural elevation of the earth's surface", 8),
                Word(0, "democracy", "A system of government by the people", 9),
                Word(0, "astronaut", "A person trained to travel in a spacecraft", 9),
                Word(0, "telephone", "A device for voice communication", 9),
                Word(0, "basketball", "A team game played with a ball and hoop", 10),
                Word(0, "television", "An electronic system of transmitting images", 10),
                Word(0, "university", "An educational institution", 10)
            )
            wordDao.insertAll(wordsList)
        }
    }
}
