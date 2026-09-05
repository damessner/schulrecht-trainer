package at.schulrecht.trainer.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SrsTest {
    private val day = 24L * 60 * 60 * 1000

    @Test
    fun wrongResetsToBoxZeroWithOneDay() {
        val n = Srs.next(correct = false, box = 3, passes = 2, fails = 0, now = 1000L)
        assertEquals(0, n.box)
        assertEquals(1000L + day, n.nextDue)
        assertEquals(1, n.fails)
        assertEquals(2, n.passes)
    }

    @Test
    fun correctAdvancesBox() {
        val n = Srs.next(correct = true, box = 0, passes = 0, fails = 1, now = 0L)
        assertEquals(1, n.box)
        assertEquals(3 * day, n.nextDue)
    }

    @Test
    fun boxCapsAtMaxInterval() {
        val n = Srs.next(correct = true, box = 4, passes = 9, fails = 0, now = 0L)
        assertEquals(4, n.box)
        assertEquals(30 * day, n.nextDue)
        assertTrue(n.passes == 10)
    }
}
