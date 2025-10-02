package xyz.malefic.frc.pingu.command

import com.pathplanner.lib.auto.NamedCommands
import edu.wpi.first.wpilibj2.command.Command

/**
 * DSL receiver for [Commangu.registerCommands].
 *
 * Provides a [bind] function to register [Command]s by name.
 */
class CommanguDsl {
    /**
     * Registers a [Command] with the given name.
     *
     * @param name The name to register the [Command] under.
     * @param command The [Command] to register.
     */
    fun bind(
        name: String,
        command: Command,
    ) {
        NamedCommands.registerCommand(name, command)
    }
}
