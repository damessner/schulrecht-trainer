package at.schulrecht.trainer.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AppVersionTest {
    @Test
    fun newerMinor() {
        assertEquals(true, AppVersion.isNewer("v1.4", "1.3.0"))
    }

    @Test
    fun sameIsNotNewer() {
        assertEquals(false, AppVersion.isNewer("v1.3", "1.3.0"))
    }

    @Test
    fun olderIsNotNewer() {
        assertEquals(false, AppVersion.isNewer("v1.1", "1.3.0"))
    }

    @Test
    fun newerPatch() {
        assertEquals(true, AppVersion.isNewer("v1.3.1", "1.3.0"))
    }
}
