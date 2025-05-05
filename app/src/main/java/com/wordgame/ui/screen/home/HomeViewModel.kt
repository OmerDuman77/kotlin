package com.wordgame.ui.screen.home

import androidx.lifecycle.ViewModel
import com.wordgame.data.db.entity.GameResult
import com.wordgame.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {
    
    val topScores: Flow<List<GameResult>> = gameRepository.getTopResults()
}
