package at.schulrecht.trainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import at.schulrecht.trainer.ui.exam.ExamScreen
import at.schulrecht.trainer.ui.exam.ExamViewModel
import at.schulrecht.trainer.ui.home.HomeScreen
import at.schulrecht.trainer.ui.home.HomeViewModel
import at.schulrecht.trainer.ui.module.ModuleScreen
import at.schulrecht.trainer.ui.module.ModuleViewModel
import at.schulrecht.trainer.ui.quiz.QuizScreen
import at.schulrecht.trainer.ui.quiz.QuizViewModel
import at.schulrecht.trainer.ui.theme.TrainerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as TrainerApp).container
        setContent {
            TrainerTheme {
                val nav = rememberNavController()
                NavHost(navController = nav, startDestination = "home") {
                    composable("home") {
                        val vm: HomeViewModel = viewModel(
                            factory = factory { HomeViewModel(container.repository) }
                        )
                        HomeScreen(
                            viewModel = vm,
                            onOpenModule = { nav.navigate("module/$it") },
                            onOpenReview = { nav.navigate("review") }
                        )
                    }
                    composable(
                        "module/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStack ->
                        val id = backStack.arguments?.getString("id") ?: return@composable
                        val vm: ModuleViewModel = viewModel(
                            key = "module-$id",
                            factory = factory { ModuleViewModel(id, container.repository) }
                        )
                        ModuleScreen(
                            moduleId = id,
                            viewModel = vm,
                            onBack = { nav.popBackStack() },
                            onStartQuiz = { mid, level -> nav.navigate("quiz/$mid/$level") },
                            onStartExam = { mid -> nav.navigate("exam/$mid") }
                        )
                    }
                    composable("review") {
                        val vm: QuizViewModel = viewModel(
                            key = "review-${System.currentTimeMillis()}",
                            factory = factory {
                                QuizViewModel("", "", container.repository, reviewOnly = true)
                            }
                        )
                        QuizScreen(
                            moduleId = "Wiederholen",
                            level = "fällig",
                            viewModel = vm,
                            onBack = { nav.popBackStack() }
                        )
                    }
                    composable(
                        "exam/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStack ->
                        val id = backStack.arguments?.getString("id") ?: return@composable
                        val vm: ExamViewModel = viewModel(
                            key = "exam-$id-${System.currentTimeMillis()}",
                            factory = factory { ExamViewModel(id, container.repository) }
                        )
                        ExamScreen(
                            moduleId = id,
                            viewModel = vm,
                            onBack = { nav.popBackStack() }
                        )
                    }
                    composable(
                        "quiz/{id}/{level}",
                        arguments = listOf(
                            navArgument("id") { type = NavType.StringType },
                            navArgument("level") { type = NavType.StringType }
                        )
                    ) { backStack ->
                        val id = backStack.arguments?.getString("id") ?: return@composable
                        val level = backStack.arguments?.getString("level") ?: return@composable
                        val vm: QuizViewModel = viewModel(
                            key = "quiz-$id-$level-${System.currentTimeMillis()}",
                            factory = factory { QuizViewModel(id, level, container.repository) }
                        )
                        QuizScreen(
                            moduleId = id,
                            level = level,
                            viewModel = vm,
                            onBack = { nav.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    private fun <T : ViewModel> factory(create: () -> T): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <V : ViewModel> create(modelClass: Class<V>): V = create() as V
        }
}
