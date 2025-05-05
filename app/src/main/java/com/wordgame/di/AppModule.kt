package com.wordgame.di

import android.content.Context
import androidx.room.Room
import com.wordgame.data.db.WordGameDatabase
import com.wordgame.data.db.dao.GameResultDao
import com.wordgame.data.db.dao.WordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WordGameDatabase {
        return Room.databaseBuilder(
            context,
            WordGameDatabase::class.java,
            "word_game_database"
        ).build()
    }
    
    @Provides
    fun provideWordDao(database: WordGameDatabase): WordDao {
        return database.wordDao()
    }
    
    @Provides
    fun provideGameResultDao(database: WordGameDatabase): GameResultDao {
        return database.gameResultDao()
    }
}