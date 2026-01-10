package xyz.malefic.frc.statemachine

/**
 * Generic state machine for subsystems.
 *
 * Manages state transitions with history tracking and validation.
 *
 * Example:
 * ```kotlin
 * enum class ElevatorState(val position: Double) {
 *     DEFAULT(0.0),
 *     L1(0.5),
 *     L2(1.0)
 * }
 *
 * object Elevator : SubsystemBase() {
 *     private val stateMachine = StateMachine(ElevatorState.DEFAULT)
 *
 *     var state: ElevatorState
 *         get() = stateMachine.currentState
 *         set(value) = stateMachine.transitionTo(value)
 *
 *     override fun periodic() {
 *         // Execute behavior based on current state
 *         setPosition(state.position)
 *     }
 * }
 * ```
 *
 * @param S State type (typically an enum)
 * @param initialState Starting state
 */
class StateMachine<S>(
    initialState: S,
) {
    /**
     * Current state of the machine
     */
    var currentState: S = initialState
        private set

    /**
     * History of previous states (for debugging and rollback)
     */
    private val stateHistory = mutableListOf<S>()

    /**
     * Optional: Callback invoked on every state transition
     */
    var onTransition: ((from: S, to: S) -> Unit)? = null

    /**
     * Transition to a new state.
     *
     * @param newState The state to transition to
     */
    fun transitionTo(newState: S) {
        if (currentState != newState) {
            val oldState = currentState
            stateHistory.add(currentState)
            currentState = newState
            onTransition?.invoke(oldState, newState)
        }
    }

    /**
     * Get the previous state (if any).
     *
     * @return Previous state, or null if no history
     */
    fun previousState(): S? = stateHistory.lastOrNull()

    /**
     * Get full state history.
     *
     * @return List of previous states (oldest first)
     */
    fun history(): List<S> = stateHistory.toList()

    /**
     * Clear state history (useful for testing or memory management).
     */
    fun clearHistory() {
        stateHistory.clear()
    }
}
