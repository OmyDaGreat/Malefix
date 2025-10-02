package xyz.malefic.frc.pingu.command

import com.pathplanner.lib.auto.NamedCommands.registerCommand
import edu.wpi.first.wpilibj2.command.Command

/**
 * A utility object for binding [Command]s with names.
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
     * @param block Lambda with receiver for [CommanguDsl] to register commands.
     */
    fun registerCommands(block: CommanguDsl.() -> Unit) {
        CommanguDsl().apply(block)
    }
}
