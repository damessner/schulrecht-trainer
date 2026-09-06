package at.schulrecht.trainer

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import at.schulrecht.trainer.domain.AppVersion
import at.schulrecht.trainer.ui.home.HomeScreen
import at.schulrecht.trainer.ui.home.HomeViewModel
import at.schulrecht.trainer.ui.module.ModuleScreen
import at.schulrecht.trainer.ui.module.ModuleViewModel
import at.schulrecht.trainer.ui.quiz.QuizScreen
import at.schulrecht.trainer.ui.quiz.QuizViewModel
import at.schulrecht.trainer.ui.theme.TrainerTheme

class MainActivity : ComponentActivity() {
    private var updateDownloadId: Long = -1

    companion object {
        private const val KEY_DOWNLOAD_ID = "download_id"
        private const val KEY_EXPECTED_VERSION = "expected_version"
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != updateDownloadId) return
            installApk()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        updateDownloadId = updaterPrefs().getLong(KEY_DOWNLOAD_ID, -1)
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
                            onInstallAppUpdate = { url, version -> downloadUpdate(url, version) }
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

    override fun onResume() {
        super.onResume()
        if (updateDownloadId != -1L && isDownloadComplete(updateDownloadId)) {
            installApk()
        }
    }

    private fun updaterPrefs() =
        getSharedPreferences("updater", MODE_PRIVATE)

    private fun isDownloadComplete(id: Long): Boolean {
        val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        manager.query(DownloadManager.Query().setFilterById(id)).use { cursor ->
            if (!cursor.moveToFirst()) return false
            val status = cursor.getInt(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            )
            return status == DownloadManager.STATUS_SUCCESSFUL
        }
    }

    private fun downloadUpdate(apkUrl: String, expectedVersion: String) {
        if (!packageManager.canRequestPackageInstalls()) {
            Toast.makeText(
                this,
                "Bitte Installation erlauben, dann erneut tippen.",
                Toast.LENGTH_LONG
            ).show()
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:$packageName")
                )
            )
            return
        }
        try {
            val dest = java.io.File(
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "update.apk"
            )
            if (dest.exists()) dest.delete()
            updaterPrefs().edit().putString(KEY_EXPECTED_VERSION, expectedVersion).apply()
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
            updaterPrefs().edit().putLong(KEY_DOWNLOAD_ID, updateDownloadId).apply()
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
        val expected = updaterPrefs().getString(KEY_EXPECTED_VERSION, null)
        @Suppress("DEPRECATION")
        val info = if (Build.VERSION.SDK_INT >= 33) {
            packageManager.getPackageArchiveInfo(
                file.path,
                android.content.pm.PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            packageManager.getPackageArchiveInfo(file.path, 0)
        }
        val actual = info?.versionName
        if (actual == null || expected == null || !AppVersion.sameVersion(expected, actual)) {
            file.delete()
            updaterPrefs().edit()
                .remove(KEY_DOWNLOAD_ID)
                .remove(KEY_EXPECTED_VERSION)
                .apply()
            updateDownloadId = -1
            Toast.makeText(
                this,
                "Update-Datei ungültig (gefunden: ${actual ?: "?"}), bitte erneut laden.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        updaterPrefs().edit().remove(KEY_DOWNLOAD_ID).apply()
        updateDownloadId = -1
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
