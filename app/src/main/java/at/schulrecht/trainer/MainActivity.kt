package at.schulrecht.trainer

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.FileProvider
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
    private var updateDownloadId: Long = -1

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != updateDownloadId) return
            installApk()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.core.content.ContextCompat.registerReceiver(
            this,
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            androidx.core.content.ContextCompat.RECEIVER_EXPORTED
        )
        val container = (application as TrainerApp).container
        setContent {
            TrainerTheme {
                val nav = rememberNavController()
                NavHost(navController = nav, startDestination = "home") {
                    composable("home") {
                        val vm: HomeViewModel = viewModel(
                            factory = factory {
                                HomeViewModel(container.repository, BuildConfig.VERSION_NAME)
                            }
                        )
                        HomeScreen(
                            viewModel = vm,
                            onOpenModule = { nav.navigate("module/$it") },
                            onOpenReview = { nav.navigate("review") },
                            onInstallAppUpdate = { url -> downloadUpdate(url) }
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

    private fun downloadUpdate(apkUrl: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(apkUrl))
                .setTitle("Schulrecht Trainer Update")
                .setDestinationInExternalFilesDir(
                    this,
                    Environment.DIRECTORY_DOWNLOADS,
                    "update.apk"
                )
                .setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
            val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            updateDownloadId = manager.enqueue(request)
            Toast.makeText(this, "Update wird geladen …", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Download fehlgeschlagen.", Toast.LENGTH_LONG).show()
        }
    }

    private fun installApk() {
        val file = java.io.File(
            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "update.apk"
        )
        if (!file.exists()) {
            Toast.makeText(this, "Download fehlgeschlagen.", Toast.LENGTH_LONG).show()
            return
        }
        val apkUri = FileProvider.getUriForFile(this, "$packageName.provider", file)
        val install = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(install)
    }

    override fun onDestroy() {
        unregisterReceiver(downloadReceiver)
        super.onDestroy()
    }
}
