package at.schulrecht.trainer.domain

object AppVersion {
    fun isNewer(remoteTag: String, localVersion: String): Boolean {
        val remote = parts(remoteTag)
        val local = parts(localVersion)
        val len = maxOf(remote.size, local.size)
        for (i in 0 until len) {
            val r = remote.getOrElse(i) { 0 }
            val l = local.getOrElse(i) { 0 }
            if (r != l) return r > l
        }
        return false
    }

    private fun parts(version: String): List<Int> =
        version.trim().removePrefix("v").removePrefix("V")
            .split(".", "-", "+")
            .mapNotNull { it.toIntOrNull() }

    fun sameVersion(a: String, b: String): Boolean {
        fun norm(v: String): List<Int> =
            v.trim().removePrefix("v").removePrefix("V")
                .split(".", "-", "+")
                .map { it.toIntOrNull() ?: 0 }
        val x = norm(a)
        val y = norm(b)
        val len = maxOf(x.size, y.size)
        return (0 until len).all { (x.getOrElse(it) { 0 } == y.getOrElse(it) { 0 }) }
    }
}
