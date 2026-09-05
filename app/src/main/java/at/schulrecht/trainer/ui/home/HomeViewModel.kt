package at.schulrecht.trainer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.schulrecht.trainer.data.AppUpdate
import at.schulrecht.trainer.data.ModuleUi
import at.schulrecht.trainer.data.SchulrechtRepository
import at.schulrecht.trainer.data.SyncProgress
import at.schulrecht.trainer.domain.Gamification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameState(
    val xp: Int = 0,
    val level: Int = 1,
    val xpInLevel: Int = 0,
    val xpForNext: Int = 1,
    val streak: Int = 0,
    val badges: Set<String> = emptySet()
)

data class HomeUiState(
    val modules: List<ModuleUi> = emptyList(),
    val query: String = "",
    val searchOpen: Boolean = false,
    val dueCount: Int = 0,
    val game: GameState = GameState(),
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
            launch {
                combine(
                    repo.observeAttempts(),
                    repo.observeExamsPassed()
                ) { attempts, passed -> attempts to passed }.collect { (attempts, passed) ->
                    val xp = attempts.sumOf { Gamification.xpForScore(it.score) }
                    val fullHits = attempts.count { it.correct }
                    val streak = Gamification.streakDays(attempts.map { it.createdAt })
                    _state.update { current ->
                        val completed = current.modules.count { m ->
                            m.total > 0 && m.answered >= m.total
                        }
                        current.copy(
                            game = GameState(
                                xp = xp,
                                level = Gamification.levelForXp(xp),
                                xpInLevel = Gamification.xpIntoLevel(xp).first,
                                xpForNext = Gamification.xpIntoLevel(xp).second,
                                streak = streak,
                                badges = Gamification.earnedBadges(
                                    fullHits, xp, streak, passed, completed
                                )
                            )
                        )
                    }
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

    fun toggleSearch() {
        _state.update { it.copy(searchOpen = !it.searchOpen, query = "") }
    }
}
