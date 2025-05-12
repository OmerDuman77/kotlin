WordRaceGame

Welcome to WordRaceGame, an engaging and addictive word guessing game built with Kotlin and Jetpack Compose!
Test your vocabulary skills by guessing words based on hints within a 240-second time limit.
Compete against yourself to achieve the highest score, with a leaderboard showcasing the top games ever played.
Inspired by classic word games and modern Android development practices, this project is perfect for learning and fun! 🚀

Features:
Leaderboard: View the highest-scoring games on the home screen.

Timed Gameplay: Each game lasts 240 seconds, challenging you to think fast.

Rich Word Database: 200 words (25 words each for 3 to 10 letters) stored in a Room database.

Dynamic Word Selection: 8 random words per game (one from each letter category: 3 to 10).

Hint System: Reveal a letter for a 100-point deduction, balancing strategy and speed.


Scoring System:

100 points per letter for correct words (e.g., a 10-letter word yields 1000 points if no hints used).

50 points per remaining second as a bonus (e.g., finishing with 120 seconds left adds 6000 points).

Modern UI: Built with Jetpack Compose for a sleek, responsive interface.

Persistent Storage: Scores and game history saved using Room database.



Dependencies:

The project uses the following libraries:


Jetpack Compose: For modern UI development (androidx.compose.ui, material3, runtime-livedata).

Room: For persistent storage (androidx.room.runtime, room-ktx).

Coroutines: For asynchronous operations (kotlinx-coroutines-android).

Lifecycle & ViewModel: For MVVM architecture (androidx.lifecycle.viewmodel-compose).

Core KTX & Activity Compose: For Android utilities (androidx.core.ktx, activity-compose).




Built with ❤️ for learning and fun!

Happy guessing, and may your scores be sky-high! 🚀
