package at.schulrecht.trainer.domain

object QuizScoring {
    fun scoreOf(typ: String, correct: Set<Int>, selected: Set<Int>): Float {
        if (correct.isEmpty()) return 0f
        return when (typ) {
            "multiple" -> {
                val hits = selected.intersect(correct).size
                val falseAlarms = (selected - correct).size
                ((hits - falseAlarms).toFloat() / correct.size).coerceIn(0f, 1f)
            }
            else -> if (selected == correct) 1f else 0f
        }
    }

    fun isFullyCorrect(typ: String, correct: Set<Int>, selected: Set<Int>): Boolean =
        scoreOf(typ, correct, selected) == 1f
}
