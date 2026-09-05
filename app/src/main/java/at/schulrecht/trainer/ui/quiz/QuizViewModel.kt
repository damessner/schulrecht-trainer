package at.schulrecht.trainer.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.schulrecht.trainer.data.QuestionUi
import at.schulrecht.trainer.data.SchulrechtRepository
import at.schulrecht.trainer.domain.QuizScoring
import at.schulrecht.trainer.domain.QuizShuffle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizUiState(
    val questions: List<QuestionUi> = emptyList(),
    val index: Int = 0,
    val selected: Set<Int> = emptySet(),
    val revealed: Boolean = false,
    val lastScore: Float = 0f,
    val totalScore: Float = 0f,
    val fullHits: Int = 0,
    val finished: Boolean = false,
    val loading: Boolean = true
) {
    val current: QuestionUi? get() = questions.getOrNull(index)
}

class QuizViewModel(
    private val moduleId: String,
    private val level: String,
    private val repo: SchulrechtRepository,
    private val reviewOnly: Boolean = false
) : ViewModel() {
    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val loaded = if (reviewOnly) {
                repo.observeDueQuestions().first()
            } else {
                repo.observeQuestions(moduleId, level).first()
            }
            val questions = loaded.shuffled().map { QuizShuffle.shuffle(it) }
            _state.update { it.copy(questions = questions, loading = false) }
        }
    }

    fun toggle(option: Int) {
        val s = _state.value
        val q = s.current ?: return
        if (s.revealed) return
        _state.update {
            if (q.typ == "multiple") {
                it.copy(selected = if (option in it.selected) it.selected - option else it.selected + option)
            } else {
                it.copy(selected = setOf(option))
            }
        }
    }

    fun reveal() {
        val s = _state.value
        val q = s.current ?: return
        if (s.revealed || s.selected.isEmpty()) return
        val score = QuizScoring.scoreOf(q.typ, q.richtig, s.selected)
        val full = score == 1f
        viewModelScope.launch {
            repo.recordAttempt(q, score, full)
            repo.recordReview(q, full)
        }
        _state.update {
            it.copy(
                revealed = true,
                lastScore = score,
                totalScore = it.totalScore + score,
                fullHits = it.fullHits + if (full) 1 else 0
            )
        }
    }

    fun next() {
        val s = _state.value
        if (s.index + 1 >= s.questions.size) {
            _state.update { it.copy(finished = true) }
        } else {
            _state.update { it.copy(index = it.index + 1, selected = emptySet(), revealed = false) }
        }
    }
}
