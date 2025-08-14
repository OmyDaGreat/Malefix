package xyz.malefic.frc.pingu

import com.pathplanner.lib.auto.NamedCommands.registerCommand
import edu.wpi.first.wpilibj2.command.Command

/**
 * A utility class for binding commands with names.
 */
class CommandPingu {
    /**
     * Binds a command to a name and registers it.
     *
     * Usage:
     * ```
     * CommandPingu().bind("name", command).bind("anotherName", anotherCommand)
     * ```
     *
     * @param name The name to bind the command to.
     * @param command The command to be registered.
     * @return The current instance of `CommandPingu` for method chaining.
     */
    fun bind(
        name: String,
        command: Command,
    ) = apply {
        registerCommand(name, command)
    }

    companion object {
        /**
         * DSL for registering multiple commands in a block.
         * Usage:
         * ```
         * CommandPingu.registerCommands {
         *     bind("name", command)
         * }
         * ```
         */
        fun registerCommands(block: CommandPinguDsl.() -> Unit) {
            CommandPinguDsl().apply(block)
        }
    }
}

/**
 * DSL receiver for CommandPingu.registerCommands.
 */
class CommandPinguDsl {
    fun bind(
        name: String,
        command: Command,
    ) {
        registerCommand(name, command)
    }
}
