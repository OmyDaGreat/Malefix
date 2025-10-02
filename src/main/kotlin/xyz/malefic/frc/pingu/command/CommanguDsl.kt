package xyz.malefic.frc.pingu.command

import com.pathplanner.lib.auto.NamedCommands
import edu.wpi.first.wpilibj2.command.Command

/**
 * DSL receiver for [Commangu.registerCommands].
 */
class CommanguDsl {
    fun bind(
        name: String,
        command: Command,
    ) {
        NamedCommands.registerCommand(name, command)
    }
}
