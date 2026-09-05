package at.schulrecht.trainer.domain

import java.util.Calendar

data class Badge(val id: String, val title: String, val desc: String)

val ALL_BADGES = listOf(
    Badge("erste_schritte", "Erste Schritte", "1 Frage voll richtig"),
    Badge("hunderter", "Hunderter", "100 Fragen voll richtig"),
    Badge("tausend_xp", "Tausender", "1.000 XP gesammelt"),
    Badge("serie3", "Serie 3", "3 Tage in Folge gelernt"),
    Badge("serie7", "Serie 7", "7 Tage in Folge gelernt"),
    Badge("pruefung", "Prüfung", "1 Prüfung bestanden"),
    Badge("komplettist", "Komplettist", "1 Modul vollständig beantwortet")
)

object Gamification {
    val LEVEL_THRESHOLDS = listOf(0, 100, 300, 600, 1000, 1500, 2100, 2800)

    fun xpForScore(score: Float): Int = (score * 10).toInt()

    fun levelForXp(xp: Int): Int =
        LEVEL_THRESHOLDS.indexOfLast { xp >= it } + 1

    fun xpIntoLevel(xp: Int): Pair<Int, Int> {
        val idx = LEVEL_THRESHOLDS.indexOfLast { xp >= it }.coerceAtLeast(0)
        val base = LEVEL_THRESHOLDS[idx]
        val next = LEVEL_THRESHOLDS.getOrElse(idx + 1) { base }
        return xp - base to (next - base).coerceAtLeast(1)
    }

    fun streakDays(timestamps: List<Long>, now: Long = System.currentTimeMillis()): Int {
        if (timestamps.isEmpty()) return 0
        val days = timestamps.map { startOfDay(it) }.toSortedSet()
        var day = startOfDay(now)
        if (day !in days) day -= 24 * 60 * 60 * 1000
        var streak = 0
        while (day in days) {
            streak++
            day -= 24 * 60 * 60 * 1000
        }
        return streak
    }

    fun earnedBadges(
        fullHits: Int,
        xp: Int,
        streak: Int,
        examsPassed: Int,
        modulesCompleted: Int
    ): Set<String> = buildSet {
        if (fullHits >= 1) add("erste_schritte")
        if (fullHits >= 100) add("hunderter")
        if (xp >= 1000) add("tausend_xp")
        if (streak >= 3) add("serie3")
        if (streak >= 7) add("serie7")
        if (examsPassed >= 1) add("pruefung")
        if (modulesCompleted >= 1) add("komplettist")
    }

    private fun startOfDay(ts: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = ts
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
