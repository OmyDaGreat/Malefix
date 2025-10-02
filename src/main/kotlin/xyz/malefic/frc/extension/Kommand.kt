package xyz.malefic.frc.extension

import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup
import edu.wpi.first.wpilibj2.command.Subsystem
import edu.wpi.first.wpilibj2.command.WaitCommand
import edu.wpi.first.wpilibj2.command.WaitUntilCommand

/**
 * Utility object for creating various WPILib [Command]s.
 */
object Kommand {
    /**
     * Creates an [InstantCommand] that executes the given function.
     *
     * @param reqs The [Subsystem]s required by the command.
     * @param function The function to execute.
     * @return An [InstantCommand] that executes the given function.
     */
    @JvmStatic
    fun cmd(
        vararg reqs: Subsystem,
        function: () -> Unit,
    ) = InstantCommand(function, *reqs)

    /**
     * Creates a [WaitCommand] to wait for a specified number of seconds.
     *
     * @param seconds The number of seconds to wait.
     * @return A [WaitCommand] that waits for the specified number of seconds.
     */
    @JvmStatic
    fun waitFor(seconds: Double) = WaitCommand(seconds)

    /**
     * Creates a [WaitUntilCommand] that waits until the given condition is true.
     *
     * @param function The condition to evaluate.
     * @return A [WaitUntilCommand] that waits until the condition is true.
     */
    @JvmStatic
    fun waitUntil(function: () -> Boolean) = WaitUntilCommand(function)

    /**
     * Creates an [InstantCommand] to cancel all running commands.
     *
     * @return An [InstantCommand] that cancels all running commands.
     */
    @JvmStatic
    fun cancelAll() = InstantCommand({ CommandScheduler.getInstance().cancelAll() })

    /**
     * Creates a [ParallelCommandGroup] from the given [Command]s.
     *
     * @param commands The [Command]s to run in parallel.
     * @return A [ParallelCommandGroup] that runs the given [Command]s in parallel.
     */
    @JvmStatic
    fun parallel(vararg commands: Command) = ParallelCommandGroup(*commands)

    /**
     * Creates a [SequentialCommandGroup] from the given [Command]s.
     *
     * @param commands The [Command]s to run sequentially.
     * @return A [SequentialCommandGroup] that runs the given [Command]s in sequence.
     */
    @JvmStatic
    fun sequential(vararg commands: Command) = SequentialCommandGroup(*commands)
}
