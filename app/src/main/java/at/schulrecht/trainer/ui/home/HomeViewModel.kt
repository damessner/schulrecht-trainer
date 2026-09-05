package at.schulrecht.trainer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.schulrecht.trainer.data.ModuleUi
import at.schulrecht.trainer.data.SchulrechtRepository
import at.schulrecht.trainer.data.SyncProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val modules: List<ModuleUi> = emptyList(),
    val isSyncing: Boolean = false,
    val progress: SyncProgress = SyncProgress(0, 0),
    val error: String? = null
)

class HomeViewModel(private val repo: SchulrechtRepository) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
    private var autoSyncTried = false

    init {
        viewModelScope.launch {
            repo.observeModuleUi()
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { modules ->
                    _state.update { it.copy(modules = modules) }
                    if (modules.isEmpty() && !autoSyncTried) {
                        autoSyncTried = true
                        sync()
                    }
                }
        }
    }

    fun sync() {
        if (_state.value.isSyncing) return
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, error = null) }
            try {
                repo.sync { p -> _state.update { it.copy(progress = p) } }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Sync fehlgeschlagen") }
            } finally {
                _state.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun resetProgress() {
        viewModelScope.launch { repo.resetProgress() }
    }
}
