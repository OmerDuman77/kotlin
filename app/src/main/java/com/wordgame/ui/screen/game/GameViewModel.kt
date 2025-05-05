package com.wordgame.ui.screen.game

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
    val totalQuestions: Int = 8, // 8 questions (from 3-letter to 10-letter words)
    val hintsUsed: Int = 0
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

    private val _timeRemaining = MutableStateFlow(120) // 120 seconds
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
            val gameWords = wordRepository.getWordsForGame()
            
            if (gameWords.isNotEmpty()) {
                _gameState.value = GameState(
                    isGameActive = true,
                    words = gameWords,
                    totalQuestions = gameWords.size
                )
                updateCurrentQuestion()
                startTimer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeRemaining.value > 0 && _gameState.value.isGameActive) {
                delay(1000)
                _timeRemaining.value -= 1
                
                // End game if time runs out
                if (_timeRemaining.value <= 0) {
                    endGame()
                }
            }
        }
    }

    private fun updateCurrentQuestion() {
        val state = _gameState.value
        if (state.currentQuestionIndex < state.words.size) {
            _currentQuestion.value = state.words[state.currentQuestionIndex]
            _revealedLetters.value = emptySet()
        } else {
            endGame()
        }
    }

    fun revealRandomLetter() {
        val currentWord = _currentQuestion.value?.word ?: return
        val revealedSet = _revealedLetters.value.toMutableSet()
        
        if (revealedSet.size < currentWord.length) {
            // Find indices that haven't been revealed yet
            val unrevealed = currentWord.indices.filter { it !in revealedSet }
            
            if (unrevealed.isNotEmpty()) {
                // Reveal a random unrevealed letter
                val randomIndex = unrevealed[Random.nextInt(unrevealed.size)]
                revealedSet.add(randomIndex)
                _revealedLetters.value = revealedSet
                
                // Update game state to track hints used
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
            // Calculate points for correct answer
            val letterPoints = currentWord.length * 100
            _score.value += letterPoints
            
            // Update correct answers count
            _gameState.value = _gameState.value.copy(
                correctAnswers = _gameState.value.correctAnswers + 1
            )
        }
        
        return isCorrect
    }

    fun nextQuestion() {
        val nextIndex = _gameState.value.currentQuestionIndex + 1
        _gameState.value = _gameState.value.copy(
            currentQuestionIndex = nextIndex
        )
        updateCurrentQuestion()
    }

    fun endGame() {
        if (!_gameState.value.isGameActive) return
        
        _gameState.value = _gameState.value.copy(isGameActive = false)
        timerJob?.cancel()
        
        // Calculate time bonus: 50 points per second remaining
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
