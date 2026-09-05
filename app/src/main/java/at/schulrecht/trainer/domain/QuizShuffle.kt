package at.schulrecht.trainer.domain

import at.schulrecht.trainer.data.QuestionUi
import kotlin.random.Random

object QuizShuffle {
    fun shuffle(question: QuestionUi, random: Random = Random.Default): QuestionUi {
        val order = question.optionen.indices.shuffled(random)
        val oldToNew = order.mapIndexed { newIndex, oldIndex -> oldIndex to newIndex }.toMap()
        return question.copy(
            optionen = order.map { question.optionen[it] },
            richtig = question.richtig.mapNotNull { oldToNew[it] }.toSet(),
            feedbacks = order.map { question.feedbacks.getOrElse(it) { "" } }
        )
    }
}
