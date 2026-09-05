package at.schulrecht.trainer

import android.app.Application
import at.schulrecht.trainer.data.SchulrechtRepository
import at.schulrecht.trainer.data.local.TrainerDatabase
import at.schulrecht.trainer.data.local.UserPrefs
import at.schulrecht.trainer.data.remote.ContentApi

class AppContainer(app: Application) {
    private val db = TrainerDatabase.build(app)
    private val prefs = UserPrefs(app)
    val repository = SchulrechtRepository(db, ContentApi(), prefs)
}

class TrainerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
