package xyz.malefic.frc.state

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StateMachineTest {
    enum class TestState {
        IDLE,
        RUNNING,
        STOPPED,
    }

    @Test
    fun testInitialState() {
        val stateMachine = StateMachine(TestState.IDLE)
        assertEquals(TestState.IDLE, stateMachine.currentState)
    }

    @Test
    fun testStateTransition() {
        val stateMachine = StateMachine(TestState.IDLE)
        stateMachine.transitionTo(TestState.RUNNING)
        assertEquals(TestState.RUNNING, stateMachine.currentState)
    }

    @Test
    fun testNoTransitionOnSameState() {
        val stateMachine = StateMachine(TestState.IDLE)
        var transitionCount = 0
        stateMachine.onTransition = { _, _ -> transitionCount++ }

        stateMachine.transitionTo(TestState.IDLE)
        assertEquals(0, transitionCount)
        assertEquals(TestState.IDLE, stateMachine.currentState)
    }

    @Test
    fun testStateHistory() {
        val stateMachine = StateMachine(TestState.IDLE)
        stateMachine.transitionTo(TestState.RUNNING)
        stateMachine.transitionTo(TestState.STOPPED)

        val history = stateMachine.history()
        assertEquals(2, history.size)
        assertEquals(TestState.IDLE, history[0])
        assertEquals(TestState.RUNNING, history[1])
    }

    @Test
    fun testPreviousState() {
        val stateMachine = StateMachine(TestState.IDLE)
        assertNull(stateMachine.previousState())

        stateMachine.transitionTo(TestState.RUNNING)
        assertEquals(TestState.IDLE, stateMachine.previousState())

        stateMachine.transitionTo(TestState.STOPPED)
        assertEquals(TestState.RUNNING, stateMachine.previousState())
    }

    @Test
    fun testClearHistory() {
        val stateMachine = StateMachine(TestState.IDLE)
        stateMachine.transitionTo(TestState.RUNNING)
        stateMachine.transitionTo(TestState.STOPPED)

        assertFalse(stateMachine.history().isEmpty())

        stateMachine.clearHistory()
        assertTrue(stateMachine.history().isEmpty())
        assertNull(stateMachine.previousState())
    }

    @Test
    fun testTransitionCallback() {
        val stateMachine = StateMachine(TestState.IDLE)
        var fromState: TestState? = null
        var toState: TestState? = null

        stateMachine.onTransition = { from, to ->
            fromState = from
            toState = to
        }

        stateMachine.transitionTo(TestState.RUNNING)

        assertEquals(TestState.IDLE, fromState)
        assertEquals(TestState.RUNNING, toState)
    }

    @Test
    fun testMultipleTransitions() {
        val stateMachine = StateMachine(TestState.IDLE)

        stateMachine.transitionTo(TestState.RUNNING)
        assertEquals(TestState.RUNNING, stateMachine.currentState)

        stateMachine.transitionTo(TestState.STOPPED)
        assertEquals(TestState.STOPPED, stateMachine.currentState)

        stateMachine.transitionTo(TestState.IDLE)
        assertEquals(TestState.IDLE, stateMachine.currentState)

        val history = stateMachine.history()
        assertEquals(3, history.size)
    }
}
