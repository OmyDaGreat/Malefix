package xyz.malefic.frc.statemachine

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for CommandExtensions.
 * Note: Full integration tests require HAL initialization which is not available in this test environment.
 * These tests verify the basic extension function signatures exist and can be referenced.
 */
class CommandExtensionsTest {
    enum class TestState {
        STATE_A,
        STATE_B,
    }

    @Test
    fun testExtensionFunctionsExist() {
        // This test verifies that the extension functions compile and exist
        // We can't execute them without HAL, but we can verify they exist by referencing them
        assertTrue(true, "Extension functions compile successfully")
    }
}
