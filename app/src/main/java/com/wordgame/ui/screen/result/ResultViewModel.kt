package com.wordgame.ui.screen.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordgame.data.db.entity.GameResult
import com.wordgame.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _highScoreRank = MutableStateFlow(-1)
    val highScoreRank = _highScoreRank.asStateFlow()
    
    private val score: Int = savedStateHandle.get<Int>("score") ?: 0
    private val correctAnswers: Int = savedStateHandle.get<Int>("correctAnswers") ?: 0
    private val totalQuestions: Int = savedStateHandle.get<Int>("totalQuestions") ?: 0
    private val timeRemaining: Int = savedStateHandle.get<Int>("timeRemaining") ?: 0
    
    init {
        viewModelScope.launch {
            calculateHighScoreRank()
        }
    }
    
    private suspend fun calculateHighScoreRank() {
        val allResults = gameRepository.getAllResults().first()
        val sortedResults = allResults.sortedByDescending { it.score }
        
        val rank = sortedResults.indexOfFirst { it.score <= score } + 1
        _highScoreRank.value = rank
    }
}
