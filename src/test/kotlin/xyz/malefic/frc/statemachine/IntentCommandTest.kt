package xyz.malefic.frc.statemachine

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for IntentCommand.
 * Note: Full integration tests require HAL initialization which is not available in this test environment.
 * These tests verify the basic structure and type system.
 */
class IntentCommandTest {
    @Test
    fun testIntentCommandDslBuilderCreation() {
        // Test that the DSL builder creates a valid IntentCommand
        val intent =
            intentCommand(
                "TestIntent",
                onStart = { /* no-op */ },
                isComplete = { true },
            )

        // Verify basic properties
        assertTrue(intent.name == "TestIntent")
        assertTrue(intent is IntentCommand)
    }

    @Test
    fun testIntentCommandCanExecuteDefaultTrue() {
        // Test that canExecute defaults to true
        val intent =
            intentCommand(
                "TestIntent",
                onStart = { /* no-op */ },
                isComplete = { true },
            )

        assertTrue(intent.canExecute())
    }

    @Test
    fun testIntentCommandCanExecuteCustom() {
        // Test that custom canExecute works
        var condition = false
        val intent =
            intentCommand(
                "TestIntent",
                canExecute = { condition },
                onStart = { /* no-op */ },
                isComplete = { true },
            )

        assertTrue(!intent.canExecute())
        condition = true
        assertTrue(intent.canExecute())
    }
}
