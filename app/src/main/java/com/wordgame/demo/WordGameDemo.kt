package com.wordgame.demo

import java.util.Scanner
import kotlin.random.Random
import kotlin.system.exitProcess

/**
 * A demo implementation of the Word Game that runs in a console environment.
 * This demonstrates the core game mechanics without requiring Android SDK.
 */
class WordGameDemo {
    private val wordsWithHints = mapOf(
        "cat" to "A small domesticated carnivorous mammal",
        "book" to "A written or printed work consisting of pages",
        "house" to "A building for human habitation",
        "table" to "A piece of furniture with a flat top",
        "music" to "Vocal or instrumental sounds combined",
        "garden" to "A plot of ground where plants are cultivated",
        "computer" to "An electronic device for storing and processing data",
        "mountain" to "A large natural elevation of the earth's surface",
        "telephone" to "A system for transmitting voices over a distance",
        "chocolate" to "A food made from roasted and ground cacao seeds"
    )
    
    private val words = wordsWithHints.keys.toList()
    private var gameScore = 0
    private var timeRemaining = 120 // 120 seconds
    private var currentWordIndex = 0
    private var revealedLetters = mutableSetOf<Int>()
    
    fun startGame() {
        println("=== WORD GAME ===")
        println("Answer with words of increasing length. Each letter is worth 100 points.")
        println("You have 120 seconds. Each remaining second gives you 50 extra points.")
        println("Type 'hint' to reveal a random letter (costs 50 points).")
        println("Type 'exit' to quit the game.")
        println("=== Let's Start! ===")
        
        val scanner = Scanner(System.`in`)
        
        while (currentWordIndex < words.size && timeRemaining > 0) {
            val currentWord = words[currentWordIndex]
            val hint = wordsWithHints[currentWord] ?: ""
            
            // Display current challenge
            println("\nWord ${currentWordIndex + 1}/${words.size} (${currentWord.length} letters)")
            println("Hint: $hint")
            
            // Display current word with revealed letters
            displayWordWithHints(currentWord)
            
            println("Time remaining: $timeRemaining seconds")
            print("Your answer: ")
            
            val userInput = scanner.nextLine().trim().lowercase()
            
            when {
                userInput == "exit" -> {
                    println("Game ended. Final score: $gameScore")
                    exitProcess(0)
                }
                userInput == "hint" -> {
                    useHint(currentWord)
                }
                userInput == currentWord -> {
                    println("Correct! +${currentWord.length * 100} points")
                    gameScore += currentWord.length * 100
                    currentWordIndex++
                    revealedLetters.clear()
                    timeRemaining -= 5 // Each word costs 5 seconds
                }
                else -> {
                    println("Incorrect. Try again or type 'hint' for help.")
                    timeRemaining -= 3 // Each wrong attempt costs 3 seconds
                }
            }
            
            // Simulate time passing
            timeRemaining -= 2
            
            if (timeRemaining <= 0) {
                println("\nTime's up!")
                break
            }
        }
        
        // Game over, calculate final score
        val timeBonus = timeRemaining * 50
        gameScore += timeBonus
        
        println("\n=== Game Over ===")
        println("Word score: ${gameScore - timeBonus}")
        println("Time bonus: $timeBonus")
        println("Final score: $gameScore")
        
        if (currentWordIndex >= words.size) {
            println("Congratulations! You completed all words!")
        } else {
            println("You completed $currentWordIndex out of ${words.size} words.")
        }
    }
    
    private fun displayWordWithHints(word: String) {
        val display = CharArray(word.length) { '_' }
        
        // Fill in revealed letters
        for (index in revealedLetters) {
            if (index < word.length) {
                display[index] = word[index]
            }
        }
        
        println(display.joinToString(" "))
    }
    
    private fun useHint(word: String) {
        if (revealedLetters.size >= word.length) {
            println("All letters are already revealed!")
            return
        }
        
        if (gameScore < 50) {
            println("Not enough points for a hint! You need 50 points.")
            return
        }
        
        // Find unrevealed letters
        val unrevealed = word.indices.filter { it !in revealedLetters }
        
        if (unrevealed.isNotEmpty()) {
            val randomIndex = unrevealed.random()
            revealedLetters.add(randomIndex)
            gameScore -= 50 // Cost of hint
            
            println("Letter revealed! -50 points")
        }
    }
}

fun main() {
    val game = WordGameDemo()
    game.startGame()
}