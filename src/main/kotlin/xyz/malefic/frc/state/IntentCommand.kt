package xyz.malefic.frc.state

import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.Subsystem

/**
 * Base class for intent-based commands that coordinate multiple subsystems.
 *
 * Combines the clarity of "intent" with the power of WPILib Commands:
 * - Clear semantic meaning (e.g., "ScoreCoralL2Intent")
 * - Automatic requirements management via CommandScheduler
 * - Composable with other commands (sequential, parallel, race, deadline)
 *
 * ## Architecture
 *
 * IntentCommands coordinate **state transitions** across multiple subsystems:
 * ```
 * User Intent → IntentCommand → Transitions State(s) → State Machine Executes
 *      ↓              ↓                  ↓                      ↓
 *  "Score L2" [ScoreCoralIntent]    [Set states]     [Run motors continuously]
 * ```
 *
 * The command sets subsystem states, then finishes when the goal is achieved.
 * Subsystem state machines continue running in `periodic()` to maintain behaviors.
 *
 * ## Example Usage
 *
 * ```kotlin
 * class ScoreCoralIntent(val level: ElevatorState) : IntentCommand(Elevator, Outtake) {
 *     override fun canExecute() = Outtake.hasCoralPiece
 *
 *     override fun onStart() {
 *         Elevator.state = level
 *         Outtake.state = OuttakeState.PREPARE_SCORE
 *     }
 *
 *     override fun isIntentComplete() =
 *         Elevator.isAtTarget() && Outtake.isReady()
 * }
 *
 * // Usage
 * button.onTrue(ScoreCoralIntent(ElevatorState.L2))
 * ```
 *
 * @param requirements Subsystems required by this intent (passed to Command constructor)
 */
abstract class IntentCommand(
    vararg requirements: Subsystem,
) : Command() {
    init {
        addRequirements(*requirements)
    }

    /**
     * Check if prerequisites are met before this intent can execute.
     *
     * This is called during [initialize] before [onStart]. If this returns false,
     * the command will immediately cancel itself.
     *
     * Use this for:
     * - Checking sensor states (e.g., "do we have a game piece?")
     * - Validating subsystem readiness (e.g., "is mechanism homed?")
     * - Safety checks (e.g., "is it safe to move?")
     *
     * @return true if the intent can execute, false to cancel
     */
    open fun canExecute(): Boolean = true

    /**
     * Configure subsystem states when intent starts.
     *
     * This is called once when the command is scheduled (after [canExecute] check).
     * Set all required subsystem states here atomically.
     *
     * Example:
     * ```kotlin
     * override fun onStart() {
     *     Elevator.state = targetLevel
     *     Arm.state = ArmState.SCORE
     *     Intake.state = IntakeState.HOLD
     * }
     * ```
     */
    abstract fun onStart()

    /**
     * Optional: Update during execution.
     *
     * Called every ~20ms while the command runs. Most IntentCommands don't need this
     * since subsystems handle their own behavior in `periodic()`.
     *
     * Use this for:
     * - Dynamic adjustments based on sensors
     * - Coordinated state transitions during execution
     * - Monitoring and logging
     */
    open fun onExecute() {}

    /**
     * Check if intent goal is achieved.
     *
     * Return true when all subsystems have reached their target states.
     * The command will finish when this returns true.
     *
     * Example:
     * ```kotlin
     * override fun isIntentComplete() =
     *     Elevator.isAtTarget() && Arm.isAtTarget()
     * ```
     *
     * @return true when the intent's goal is complete
     */
    abstract fun isIntentComplete(): Boolean

    /**
     * Cleanup/safety when interrupted or finished.
     *
     * Called when the command ends, either by completing normally or being interrupted.
     *
     * @param interrupted true if interrupted, false if completed normally
     */
    open fun onFinish(interrupted: Boolean) {}

    // WPILib Command interface implementation
    final override fun initialize() {
        if (!canExecute()) {
            cancel()
            return
        }
        onStart()
    }

    final override fun execute() {
        onExecute()
    }

    final override fun isFinished(): Boolean = isIntentComplete()

    final override fun end(interrupted: Boolean) {
        onFinish(interrupted)
    }
}

/**
 * DSL-style builder for simple intent commands without creating a class.
 *
 * Use this for simple, one-off intents that don't need a dedicated class.
 *
 * Example:
 * ```kotlin
 * val stowIntent = intentCommand(
 *     "Stow",
 *     Elevator, Arm,
 *     onStart = {
 *         Elevator.state = ElevatorState.DEFAULT
 *         Arm.state = ArmState.STOW
 *     },
 *     isComplete = { Elevator.isAtTarget() && Arm.isAtTarget() }
 * )
 *
 * button.onTrue(stowIntent)
 * ```
 *
 * @param name Human-readable name for the intent
 * @param requirements Subsystems required by this intent
 * @param canExecute Lambda to check prerequisites (default: always true)
 * @param onStart Lambda to configure states when intent starts
 * @param isComplete Lambda to check if intent goal is achieved
 * @param onFinish Lambda for cleanup (default: no-op)
 * @return An IntentCommand instance
 */
fun intentCommand(
    name: String,
    vararg requirements: Subsystem,
    canExecute: () -> Boolean = { true },
    onStart: () -> Unit,
    isComplete: () -> Boolean,
    onFinish: (Boolean) -> Unit = {},
): IntentCommand =
    object : IntentCommand(*requirements) {
        init {
            this.name = name
        }

        override fun canExecute(): Boolean = canExecute()

        override fun onStart() = onStart()

        override fun isIntentComplete(): Boolean = isComplete()

        override fun onFinish(interrupted: Boolean) = onFinish(interrupted)
    }
