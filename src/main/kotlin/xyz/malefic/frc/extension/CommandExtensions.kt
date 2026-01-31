package xyz.malefic.frc.extension

import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler

/**
 * Schedule this command when the provided [condition] is true.
 *
 * @param condition If true, the command will be scheduled using the global [CommandScheduler].
 */
fun Command.scheduleIf(condition: Boolean) {
    if (condition) {
        this()
    }
}

/**
 * Operator invoke implementation for [Command].
 *
 * Schedules this command using the global [CommandScheduler]. Allows calling
 * the command with a function-call style: `myCommand()`.
 */
operator fun Command.invoke() = CommandScheduler.getInstance().schedule(this)
