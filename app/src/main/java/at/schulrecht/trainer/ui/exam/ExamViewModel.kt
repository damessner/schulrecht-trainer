package at.schulrecht.trainer.ui.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.schulrecht.trainer.data.QuestionUi
import at.schulrecht.trainer.data.SchulrechtRepository
import at.schulrecht.trainer.domain.QuizScoring
import at.schulrecht.trainer.domain.QuizShuffle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExamResult(
    val question: QuestionUi,
    val selected: Set<Int>,
    val score: Float
)

data class ExamUiState(
    val questions: List<QuestionUi> = emptyList(),
    val index: Int = 0,
    val answers: Map<String, Set<Int>> = emptyMap(),
    val remainingSeconds: Int = 0,
    val submitted: Boolean = false,
    val results: List<ExamResult> = emptyList(),
    val loading: Boolean = true
) {
    val current: QuestionUi? get() = questions.getOrNull(index)
    val totalScore: Float get() = results.sumOf { it.score.toDouble() }.toFloat()
    val passed: Boolean get() = questions.isNotEmpty() &&
        totalScore / questions.size >= PASS_THRESHOLD
}

const val EXAM_QUESTIONS = 20
const val EXAM_SECONDS_PER_QUESTION = 60
const val PASS_THRESHOLD = 0.6f

class ExamViewModel(
    moduleId: String,
    private val repo: SchulrechtRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ExamUiState())
    val state: StateFlow<ExamUiState> = _state.asStateFlow()
    private var timerStarted = false

    init {
        viewModelScope.launch {
            val all = listOf("L1", "L2", "L3", "L4").flatMap { level ->
                repo.observeQuestions(moduleId, level).first()
            }
            val picked = all.shuffled().take(EXAM_QUESTIONS).map { QuizShuffle.shuffle(it) }
            _state.update {
                it.copy(
                    questions = picked,
                    remainingSeconds = picked.size * EXAM_SECONDS_PER_QUESTION,
                    loading = false
                )
            }
            startTimer()
        }
    }

    private fun startTimer() {
        if (timerStarted) return
        timerStarted = true
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val left = _state.value.remainingSeconds - 1
                if (left <= 0) {
                    _state.update { it.copy(remainingSeconds = 0) }
                    submit()
                    break
                }
                _state.update { it.copy(remainingSeconds = left) }
            }
        }
    }

    fun select(option: Int) {
        val s = _state.value
        if (s.submitted) return
        val q = s.current ?: return
        val id = q.id
        _state.update { state ->
            val current = state.answers[id].orEmpty()
            val next = if (q.typ == "multiple") {
                if (option in current) current - option else current + option
            } else {
                setOf(option)
            }
            state.copy(answers = state.answers + (id to next))
        }
    }

    fun move(delta: Int) {
        _state.update {
            val next = (it.index + delta).coerceIn(0, (it.questions.size - 1).coerceAtLeast(0))
            it.copy(index = next)
        }
    }

    fun submit() {
        val s = _state.value
        if (s.submitted || s.questions.isEmpty()) return
        val results = s.questions.map { q ->
            val selected = s.answers[q.id].orEmpty()
            ExamResult(q, selected, QuizScoring.scoreOf(q.typ, q.richtig, selected))
        }
        _state.update { it.copy(results = results, submitted = true) }
        viewModelScope.launch {
            results.forEach { r ->
                val full = r.score == 1f
                repo.recordAttempt(r.question, r.score, full)
                repo.recordReview(r.question, full)
            }
            val total = results.sumOf { it.score.toDouble() }.toFloat()
            repo.recordExam(total / s.questions.size >= PASS_THRESHOLD)
        }
    }
}
