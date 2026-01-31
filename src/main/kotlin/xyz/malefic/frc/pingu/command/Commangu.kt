package xyz.malefic.frc.pingu.command

import com.pathplanner.lib.auto.NamedCommands.registerCommand
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup
import edu.wpi.first.wpilibj2.command.ParallelRaceGroup
import edu.wpi.first.wpilibj2.command.RunCommand
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup
import edu.wpi.first.wpilibj2.command.Subsystem
import edu.wpi.first.wpilibj2.command.WaitCommand
import edu.wpi.first.wpilibj2.command.WaitUntilCommand

/**
 * Utility object for creating various WPILib commands and binding them with names.
 */
object Commangu {
    /**
     * Binds a [Command] to a name and registers it.
     *
     * Usage:
     * ```kotlin
     * Commangu.bind("name", command).bind("anotherName", anotherCommand)
     * ```
     *
     * @param name The name to bind the [Command] to.
     * @param command The [Command] to be registered.
     * @return The current instance of [Commangu] for method chaining.
     */
    fun bind(
        name: String,
        command: Command,
    ) = apply {
        registerCommand(name, command)
    }

    /**
     * DSL for registering multiple [Command]s in a block.
     *
     * Usage:
     * ```kotlin
     * Commangu.registerCommands {
     *     bind("name", command)
     * }
     * ```
     *
     * @param block Lambda with receiver for [BindDsl] to register commands.
     */
    fun registerCommands(block: BindDsl.() -> Unit) {
        BindDsl().apply(block)
    }

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
     * Creates a [RunCommand] that runs the given function.
     *
     * @param function The function to run.
     * @return A [RunCommand] that runs the given function.
     */
    @JvmStatic
    fun runUntil(
        vararg reqs: Subsystem,
        condition: () -> Boolean,
        function: () -> Unit,
    ): ParallelRaceGroup = RunCommand(function, *reqs).until { condition() }

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
