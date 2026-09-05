package at.schulrecht.trainer.domain

object Srs {
    val INTERVALS_DAYS = longArrayOf(1, 3, 7, 14, 30)

    data class Next(val box: Int, val nextDue: Long, val passes: Int, val fails: Int)

    fun next(
        correct: Boolean,
        box: Int,
        passes: Int,
        fails: Int,
        now: Long = System.currentTimeMillis()
    ): Next {
        val day = 24L * 60 * 60 * 1000
        return if (correct) {
            val newBox = (box + 1).coerceAtMost(INTERVALS_DAYS.lastIndex)
            Next(newBox, now + INTERVALS_DAYS[newBox] * day, passes + 1, fails)
        } else {
            Next(0, now + INTERVALS_DAYS[0] * day, passes, fails + 1)
        }
    }
}
