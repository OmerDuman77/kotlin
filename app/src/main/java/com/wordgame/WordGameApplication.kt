package com.wordgame

import android.app.Application
import com.wordgame.data.db.WordGameDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WordGameApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        WordGameDatabase.getInstance(applicationContext)
    }

}