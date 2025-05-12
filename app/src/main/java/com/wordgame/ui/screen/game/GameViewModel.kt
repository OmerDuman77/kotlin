package com.wordgame.ui.screen.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordgame.data.db.entity.GameResult
import com.wordgame.data.db.entity.Word
import com.wordgame.data.repository.GameRepository
import com.wordgame.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class GameState(
    val isGameActive: Boolean = false,
    val words: List<Word> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 8,
    val hintsUsed: Int = 0,
    var answerGiven: Boolean = false
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _currentQuestion = MutableStateFlow<Word?>(null)
    val currentQuestion: StateFlow<Word?> = _currentQuestion.asStateFlow()

    private val _revealedLetters = MutableStateFlow<Set<Int>>(emptySet())
    val revealedLetters: StateFlow<Set<Int>> = _revealedLetters.asStateFlow()

    private val _timeRemaining = MutableStateFlow(240)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    private var timerJob: Job? = null

    init {
        startGame()
    }

    private fun startGame() {
        viewModelScope.launch {
            Log.d("GameViewModel", "startGame() called")
            val gameWords = wordRepository.getWordsForGame()
            Log.d("GameViewModel", "startGame() got ${gameWords.size} words")
            if (gameWords.isNotEmpty()) {
                _gameState.value = GameState(
                    isGameActive = true,
                    words = gameWords,
                    totalQuestions = gameWords.size
                )
                Log.d("GameViewModel", "GameState updated with ${gameWords.size} words")
                updateCurrentQuestion()
                startTimer()
            } else {
                Log.e("GameViewModel", "Failed to load any game words")
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeRemaining.value > 0 && _gameState.value.isGameActive) {
                delay(1000)
                _timeRemaining.value -= 1

                if (_timeRemaining.value <= 0) {
                    endGame()
                }
            }
        }
    }

    private fun updateCurrentQuestion() {
        val state = _gameState.value
        Log.d("GameViewModel", "updateCurrentQuestion() called with index ${state.currentQuestionIndex}, words size ${state.words.size}")
        if (state.currentQuestionIndex < state.words.size) {
            _currentQuestion.value = state.words[state.currentQuestionIndex]
            Log.d("GameViewModel", "Current question set to ${_currentQuestion.value?.word}")
            _revealedLetters.value = emptySet()
        } else {
            Log.d("GameViewModel", "End of questions reached")
            endGame()
        }
    }

    fun revealRandomLetter() {
        val currentWord = _currentQuestion.value?.word ?: return
        val revealedSet = _revealedLetters.value.toMutableSet()

        if (revealedSet.size < currentWord.length) {
            val unrevealed = currentWord.indices.filter { it !in revealedSet }

            if (unrevealed.isNotEmpty()) {
                val randomIndex = unrevealed[Random.nextInt(unrevealed.size)]
                revealedSet.add(randomIndex)
                _revealedLetters.value = revealedSet

                _gameState.value = _gameState.value.copy(
                    hintsUsed = _gameState.value.hintsUsed + 1
                )
            }
        }
    }


    fun checkAnswer(answer: String): Boolean {
        val currentWord = _currentQuestion.value?.word ?: return false
        val isCorrect = answer.trim().equals(currentWord, ignoreCase = true)

        if (isCorrect) {
            if (!_gameState.value.answerGiven) {
                val totalPoints = currentWord.length * 100
                val revealedPoints = _revealedLetters.value.size * 100
                val earnedPoints = totalPoints - revealedPoints
                _score.value += earnedPoints

                _gameState.value = _gameState.value.copy(
                    correctAnswers = _gameState.value.correctAnswers + 1,
                    answerGiven = true
                )
            }
        }

        return isCorrect
    }

    fun nextQuestion() {
        val nextIndex = _gameState.value.currentQuestionIndex + 1
        _gameState.value = _gameState.value.copy(
            currentQuestionIndex = nextIndex,
                    answerGiven = false
        )
        updateCurrentQuestion()
    }

    fun endGame() {
        if (!_gameState.value.isGameActive) return
        
        _gameState.value = _gameState.value.copy(isGameActive = false)
        timerJob?.cancel()

        val timeBonus = _timeRemaining.value * 50
        _score.value += timeBonus
        
        viewModelScope.launch {
            val gameResult = GameResult(
                score = _score.value,
                correctAnswers = _gameState.value.correctAnswers,
                totalQuestions = _gameState.value.totalQuestions,
                timeRemaining = _timeRemaining.value
            )
            gameRepository.saveResult(gameResult)
            _isGameOver.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
