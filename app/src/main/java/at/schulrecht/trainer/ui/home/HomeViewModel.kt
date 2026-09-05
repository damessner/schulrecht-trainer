package at.schulrecht.trainer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.schulrecht.trainer.data.AppUpdate
import at.schulrecht.trainer.data.ModuleUi
import at.schulrecht.trainer.data.SchulrechtRepository
import at.schulrecht.trainer.data.SyncProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val modules: List<ModuleUi> = emptyList(),
    val query: String = "",
    val dueCount: Int = 0,
    val isSyncing: Boolean = false,
    val progress: SyncProgress = SyncProgress(0, 0),
    val updateAvailable: Boolean = false,
    val remoteVersion: String? = null,
    val appUpdate: AppUpdate? = null,
    val error: String? = null
)

class HomeViewModel(
    private val repo: SchulrechtRepository,
    private val appVersion: String
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
    private var autoSyncTried = false

    init {
        viewModelScope.launch {
            launch {
                repo.observeDueCount().collect { due ->
                    _state.update { it.copy(dueCount = due) }
                }
            }
            repo.observeModuleUi()
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { modules ->
                    _state.update { it.copy(modules = modules) }
                    if (modules.isEmpty() && !autoSyncTried) {
                        autoSyncTried = true
                        sync()
                    } else if (modules.isNotEmpty() && !autoSyncTried) {
                        autoSyncTried = true
                        checkForUpdate()
                    }
                }
        }
    }

    fun sync() {
        if (_state.value.isSyncing) return
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, error = null, updateAvailable = false) }
            try {
                repo.sync { p -> _state.update { it.copy(progress = p) } }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Sync fehlgeschlagen") }
            } finally {
                _state.update { it.copy(isSyncing = false) }
            }
            checkForUpdate()
        }
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            try {
                val remote = repo.remoteManifestVersion()
                val local = repo.observeLocalVersion().first()
                val appUpdate = try {
                    repo.checkAppUpdate(appVersion)
                } catch (e: Exception) {
                    null
                }
                _state.update {
                    it.copy(
                        updateAvailable = local != null && remote != local,
                        remoteVersion = remote,
                        appUpdate = appUpdate
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(updateAvailable = false) }
            }
        }
    }

    fun resetProgress() {
        viewModelScope.launch { repo.resetProgress() }
    }

    fun setQuery(query: String) {
        _state.update { it.copy(query = query) }
    }
}
