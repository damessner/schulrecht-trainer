package at.schulrecht.trainer.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class QuizScoringTest {
    @Test
    fun singleCorrect() {
        assertEquals(1f, QuizScoring.scoreOf("single", setOf(1), setOf(1)))
    }

    @Test
    fun singleWrong() {
        assertEquals(0f, QuizScoring.scoreOf("single", setOf(1), setOf(2)))
    }

    @Test
    fun tfCorrect() {
        assertEquals(1f, QuizScoring.scoreOf("tf", setOf(0), setOf(0)))
    }

    @Test
    fun multiplePartial() {
        assertEquals(2f / 3f, QuizScoring.scoreOf("multiple", setOf(0, 1, 2), setOf(0, 1)))
    }

    @Test
    fun multipleFalseAlarmReduces() {
        assertEquals(2f / 3f, QuizScoring.scoreOf("multiple", setOf(0, 1, 2), setOf(0, 1, 2, 3)))
    }

    @Test
    fun multipleAllWrongClamped() {
        assertEquals(0f, QuizScoring.scoreOf("multiple", setOf(0), setOf(1, 2)))
    }

    @Test
    fun multipleFull() {
        assertEquals(true, QuizScoring.isFullyCorrect("multiple", setOf(0, 2), setOf(0, 2)))
    }
}
