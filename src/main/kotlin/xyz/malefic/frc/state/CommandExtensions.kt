package xyz.malefic.frc.state

import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.Subsystem
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Create a command that sets a subsystem state and waits for completion.
 *
 * Example:
 * ```kotlin
 * val moveToL2 = Elevator.setStateAndWait(
 *     ElevatorState.L2,
 *     stateSetter = { state = it },
 *     isAtTarget = { isAtTarget() },
 *     timeout = 3.seconds
 * )
 * ```
 *
 * @param newState The state to transition to
 * @param stateSetter Lambda to set the state
 * @param isAtTarget Lambda to check if target reached
 * @param timeout Maximum time to wait
 * @return Command that transitions and waits
 */
fun <T : Subsystem, S> T.setStateAndWait(
    newState: S,
    stateSetter: T.(S) -> Unit,
    isAtTarget: T.() -> Boolean,
    timeout: Duration = 3.seconds,
): Command =
    this
        .runOnce { stateSetter(newState) }
        .andThen(
            this
                .run {}
                .until { isAtTarget() }
                .withTimeout(timeout.inWholeMilliseconds / 1000.0),
        )

/**
 * Create an instant command that just sets state (fire-and-forget).
 *
 * The state machine will handle execution in periodic().
 *
 * Example:
 * ```kotlin
 * val stow = Elevator.setState(
 *     ElevatorState.DEFAULT,
 *     stateSetter = { state = it }
 * )
 * ```
 *
 * @param newState The state to transition to
 * @param stateSetter Lambda to set the state
 * @return InstantCommand that transitions state
 */
fun <T : Subsystem, S> T.setState(
    newState: S,
    stateSetter: T.(S) -> Unit,
): Command = this.runOnce { stateSetter(newState) }

/**
 * Wait until a condition is true with timeout.
 *
 * Example:
 * ```kotlin
 * val waitForPiece = Intake.waitUntil(
 *     condition = { hasPiece },
 *     timeout = 2.seconds
 * )
 * ```
 *
 * @param condition Lambda returning true when done waiting
 * @param timeout Maximum time to wait
 * @return Command that waits for condition
 */
fun Subsystem.waitUntil(
    condition: () -> Boolean,
    timeout: Duration = 2.seconds,
): Command =
    this
        .run {}
        .until(condition)
        .withTimeout(timeout.inWholeMilliseconds / 1000.0)

/**
 * Run a command while a condition is true.
 *
 * Example:
 * ```kotlin
 * val intakeUntilFull = Intake.runWhile(
 *     action = { runIntake() },
 *     condition = { !hasPiece }
 * )
 * ```
 *
 * @param action Lambda to execute
 * @param condition Lambda that returns true while command should run
 * @return Command that runs while condition is true
 */
fun Subsystem.runWhile(
    action: () -> Unit,
    condition: () -> Boolean,
): Command =
    this
        .run { action() }
        .until { !condition() }
