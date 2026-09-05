package at.schulrecht.trainer.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prefs by preferencesDataStore("trainer_prefs")

class UserPrefs(context: Context) {
    private val store = context.applicationContext.prefs

    val manifestVersion: Flow<String?> = store.data.map { it[KEY_VERSION] }
    val lastSync: Flow<Long> = store.data.map { it[KEY_SYNC] ?: 0L }

    suspend fun setManifestVersion(version: String) {
        store.edit { it[KEY_VERSION] = version }
    }

    suspend fun setLastSync(timestamp: Long) {
        store.edit { it[KEY_SYNC] = timestamp }
    }

    val examsTaken: Flow<Int> = store.data.map { it[KEY_EXAMS_TAKEN] ?: 0 }
    val examsPassed: Flow<Int> = store.data.map { it[KEY_EXAMS_PASSED] ?: 0 }

    suspend fun recordExam(passed: Boolean) {
        store.edit {
            it[KEY_EXAMS_TAKEN] = (it[KEY_EXAMS_TAKEN] ?: 0) + 1
            if (passed) it[KEY_EXAMS_PASSED] = (it[KEY_EXAMS_PASSED] ?: 0) + 1
        }
    }

    companion object {
        private val KEY_VERSION = stringPreferencesKey("manifest_version")
        private val KEY_SYNC = longPreferencesKey("last_sync")
        private val KEY_EXAMS_TAKEN = androidx.datastore.preferences.core.intPreferencesKey("exams_taken")
        private val KEY_EXAMS_PASSED = androidx.datastore.preferences.core.intPreferencesKey("exams_passed")
    }
}
