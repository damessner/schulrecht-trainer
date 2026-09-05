package at.schulrecht.trainer.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class GamificationTest {
    private val day = 24L * 60 * 60 * 1000

    @Test
    fun levels() {
        assertEquals(1, Gamification.levelForXp(0))
        assertEquals(2, Gamification.levelForXp(100))
        assertEquals(3, Gamification.levelForXp(300))
        assertEquals(8, Gamification.levelForXp(5000))
    }

    @Test
    fun streakCountsBackwards() {
        val now = 10 * day
        assertEquals(3, Gamification.streakDays(listOf(10 * day, 9 * day, 8 * day), now))
    }

    @Test
    fun streakToleratesYesterday() {
        val now = 10 * day
        assertEquals(2, Gamification.streakDays(listOf(9 * day, 8 * day), now))
    }

    @Test
    fun streakBreaksOnGap() {
        val now = 10 * day
        assertEquals(1, Gamification.streakDays(listOf(10 * day, 8 * day), now))
    }

    @Test
    fun badges() {
        val earned = Gamification.earnedBadges(
            fullHits = 120, xp = 1500, streak = 7, examsPassed = 1, modulesCompleted = 0
        )
        assertEquals(
            setOf("erste_schritte", "hunderter", "tausend_xp", "serie3", "serie7", "pruefung"),
            earned
        )
    }
}
