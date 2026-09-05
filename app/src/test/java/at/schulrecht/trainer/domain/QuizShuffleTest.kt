package at.schulrecht.trainer.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuizShuffleTest {
    private fun sample() = at.schulrecht.trainer.data.QuestionUi(
        id = "T-1",
        modulId = "T",
        level = "L1",
        typ = "single",
        situation = "S",
        optionen = listOf("a", "b", "c", "d"),
        richtig = setOf(1),
        feedbacks = listOf("fa", "fb", "fc", "fd"),
        aufloesung = "A",
        hauptquelle = "Q",
        zusatzquellen = emptyList(),
        stand = "04.09.2026"
    )

    @Test
    fun keepsCorrectAnswerPointingAtSameText() {
        val shuffled = QuizShuffle.shuffle(sample(), Random(42))
        val newIndex = shuffled.optionen.indexOf("b")
        assertEquals(setOf(newIndex), shuffled.richtig)
        assertEquals("fb", shuffled.feedbacks[newIndex])
        assertEquals(listOf("a", "b", "c", "d").sorted(), shuffled.optionen.sorted())
    }

    @Test
    fun actuallyMovesThings() {
        val moved = (0 until 20).any {
            QuizShuffle.shuffle(sample(), Random(it)).optionen != listOf("a", "b", "c", "d")
        }
        assertTrue(moved)
    }
}
