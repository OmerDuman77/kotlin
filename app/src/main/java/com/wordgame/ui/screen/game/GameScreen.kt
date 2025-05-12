package com.wordgame.ui.screen.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wordgame.ui.component.HintButton
import com.wordgame.ui.component.TimerView
import com.wordgame.ui.component.WordDisplayView
import com.wordgame.ui.theme.CorrectGreen
import com.wordgame.ui.theme.IncorrectRed

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onGameFinished: (Int, Int, Int, Int) -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val revealedLetters by viewModel.revealedLetters.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val score by viewModel.score.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    
    var userAnswer by remember { mutableStateOf("") }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(key1 = currentQuestion) {
        userAnswer = ""
        showFeedback = false
        if (currentQuestion != null && gameState.isGameActive) {
            focusRequester.requestFocus()
        }
    }
    
    LaunchedEffect(key1 = isGameOver) {
        if (isGameOver) {
            onGameFinished(
                score,
                gameState.correctAnswers,
                gameState.totalQuestions,
                timeRemaining
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Score",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))

                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    TimerView(
                        timeRemaining = timeRemaining,
                        totalTime = 120,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            currentQuestion?.let { question ->
                Text(
                    text = "Question ${gameState.currentQuestionIndex + 1} of ${gameState.totalQuestions}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = question.hint,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                WordDisplayView(
                    word = question.word,
                    revealedIndices = revealedLetters,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = { userAnswer = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text("Your answer") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (userAnswer.isNotBlank()) {
                                isCorrect = viewModel.checkAnswer(userAnswer)
                                showFeedback = true
                            }
                        }
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HintButton(
                        onClick = { viewModel.revealRandomLetter() },
                        enabled = revealedLetters.size < question.word.length
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            if (userAnswer.isNotBlank()) {
                                isCorrect = viewModel.checkAnswer(userAnswer)
                                showFeedback = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Submit")
                    }
                }

                if (showFeedback) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isCorrect) CorrectGreen.copy(alpha = 0.2f)
                                else IncorrectRed.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCorrect) "Correct! Well done!" 
                                   else "Incorrect. Try Again",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isCorrect) CorrectGreen else IncorrectRed
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { viewModel.nextQuestion() },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text("Next Question")
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading questions...")
                }
            }
        }
    }

    if (timeRemaining <= 0 && !isGameOver) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Time's up!") },
            text = { Text("Your time has run out. The game is over.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.endGame() }
                ) {
                    Text("See Results")
                }
            }
        )
    }
}
