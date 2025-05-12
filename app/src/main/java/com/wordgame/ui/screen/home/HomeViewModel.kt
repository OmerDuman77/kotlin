package com.wordgame.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordgame.data.db.WordGameDatabase
import com.wordgame.data.db.entity.GameResult
import com.wordgame.data.repository.GameRepository
import com.wordgame.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val wordRepository: WordRepository
) : ViewModel() {

    val topScores: Flow<List<GameResult>> = gameRepository.getTopResults()

    init {
        viewModelScope.launch {
            CoroutineScope(Dispatchers.IO).launch {
                    wordRepository.addStaticWords()
            }
        }

    }
}
