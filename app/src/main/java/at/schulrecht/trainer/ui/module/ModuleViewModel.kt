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
    val title: String = "",
    val ziele: List<String> = emptyList(),
    val levels: List<LevelStat> = emptyList()
)

val LEVEL_DESCRIPTIONS = mapOf(
    "L1" to "Grundlagen erkennen: typische Alltagssituationen richtig einordnen.",
    "L2" to "Sicher handeln: Fristen, Zuständigkeiten und Dokumentation beachten.",
    "L3" to "Grenzfälle meistern: Formfehler erkennen und korrekt reagieren.",
    "L4" to "Transfer: Wissen über Normen und Schularten hinweg anwenden."
)

class ModuleViewModel(
    private val moduleId: String,
    private val repo: SchulrechtRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ModuleUiState())
    val state: StateFlow<ModuleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val info = repo.observeModules().first().find { it.id == moduleId }
            val levels = listOf("L1", "L2", "L3", "L4").map { level ->
                val count = repo.observeQuestions(moduleId, level).first().size
                LevelStat(level, count)
            }
            _state.update {
                it.copy(
                    title = info?.titel ?: moduleId,
                    ziele = info?.let { m ->
                        if (m.zieleJson.isBlank()) emptyList()
                        else org.json.JSONArray(m.zieleJson).let { arr ->
                            List(arr.length()) { i -> arr.optString(i) }
                        }
                    } ?: emptyList(),
                    levels = levels
                )
            }
        }
    }
}
