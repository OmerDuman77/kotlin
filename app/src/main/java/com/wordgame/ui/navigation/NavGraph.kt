package com.wordgame.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wordgame.ui.screen.game.GameScreen
import com.wordgame.ui.screen.home.HomeScreen
import com.wordgame.ui.screen.result.ResultScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Game : Screen("game")
    object Result : Screen("result/{score}/{correctAnswers}/{totalQuestions}/{timeRemaining}") {
        fun createRoute(score: Int, correctAnswers: Int, totalQuestions: Int, timeRemaining: Int): String {
            return "result/$score/$correctAnswers/$totalQuestions/$timeRemaining"
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartGame = {
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Game.route) {
            GameScreen(
                onGameFinished = { score, correctAnswers, totalQuestions, timeRemaining ->
                    navController.navigate(
                        Screen.Result.createRoute(
                            score,
                            correctAnswers,
                            totalQuestions,
                            timeRemaining
                        )
                    ) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("correctAnswers") { type = NavType.IntType },
                navArgument("totalQuestions") { type = NavType.IntType },
                navArgument("timeRemaining") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val correctAnswers = backStackEntry.arguments?.getInt("correctAnswers") ?: 0
            val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 0
            val timeRemaining = backStackEntry.arguments?.getInt("timeRemaining") ?: 0
            
            ResultScreen(
                score = score,
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                timeRemaining = timeRemaining,
                onPlayAgain = {
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                },
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
