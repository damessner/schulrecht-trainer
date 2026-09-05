package at.schulrecht.trainer.ui.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.schulrecht.trainer.data.SchulrechtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LevelStat(val level: String, val total: Int)

data class ModuleUiState(
    val levels: List<LevelStat> = emptyList()
)

class ModuleViewModel(
    private val moduleId: String,
    private val repo: SchulrechtRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ModuleUiState())
    val state: StateFlow<ModuleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val levels = listOf("L1", "L2", "L3", "L4").map { level ->
                val count = repo.observeQuestions(moduleId, level).first().size
                LevelStat(level, count)
            }
            _state.update { it.copy(levels = levels) }
        }
    }
}
