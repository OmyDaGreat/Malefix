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
 * Utility object for creating various WPILib commands.
 */
object Kommand {
    /**
     * Creates an [InstantCommand] that executes the given function.
     *
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
    fun cancel() = cmd { CommandScheduler.getInstance().cancelAll() }

    /**
     * A builder class for creating a [SequentialCommandGroup].
     */
    class SequentialBuilder {
        private val commands = mutableListOf<Command>()

        /**
         * Adds a command to the sequence.
         */
        operator fun Command.unaryPlus() {
            commands.add(this)
        }

        /**
         * Builds and returns a [SequentialCommandGroup] with the added commands.
         */
        fun build(): SequentialCommandGroup = SequentialCommandGroup(*commands.toTypedArray())
    }

    /**
     * Creates a [SequentialCommandGroup] using the provided block to add commands.
     *
     * @param block The block to add commands to the sequence.
     * @return A [SequentialCommandGroup] with the added commands.
     */
    fun sequential(block: SequentialBuilder.() -> Unit): SequentialCommandGroup = SequentialBuilder().apply(block).build()

    /**
     * A builder class for creating a [ParallelCommandGroup].
     */
    class ParallelBuilder {
        private val commands = mutableListOf<Command>()

        /**
         * Adds a command to the sequence.
         */
        operator fun Command.unaryPlus() {
            commands.add(this)
        }

        /**
         * Builds and returns a [ParallelCommandGroup] with the added commands.
         */
        fun build(): ParallelCommandGroup = ParallelCommandGroup(*commands.toTypedArray())
    }

    /**
     * Creates a [ParallelCommandGroup] that runs the given commands in parallel.
     *
     * @param block The commands to run in a dsl format.
     * @return A [ParallelCommandGroup] that runs the given commands in parallel.
     */
    fun parallel(block: ParallelBuilder.() -> Unit): ParallelCommandGroup = ParallelBuilder().apply(block).build()
}
