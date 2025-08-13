package xyz.malefic.frc.pingu

import edu.wpi.first.wpilibj.XboxController
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.SubsystemBase
import xyz.malefic.frc.emu.Button

/**
 * Type alias for a Triple consisting of an XboxController, a Button, and a command supplier function.
 */
typealias ControllerButtonBinding = Triple<XboxController, Button, Pair<() -> Command, () -> Command>>

/**
 * Type alias for a Pair consisting of a Button and two command suppliers function.
 */
typealias ButtonBinding = Triple<Button, () -> Command, () -> Command>

/**
 * Bingu is a utility object for binding Xbox controller buttons to commands.
 */
object Bingu : SubsystemBase() {
    /** List to store the mappings of controllers, buttons, and command suppliers */
    private val buttonMaps: MutableList<ControllerButtonBinding> = mutableListOf()

    /**
     * Extension function for XboxController to bind multiple button-command pairs.
     *
     * @param pair Vararg of pairs where each pair consists of a Button and a command supplier function.
     */
    @JvmStatic
    @SafeVarargs
    fun XboxController.bindings(vararg pair: ButtonBinding) =
        pair.forEach { (button, pressedCommand, releasedCommand) ->
            buttonMaps.add(ControllerButtonBinding(this, button, Pair(pressedCommand, releasedCommand)))
        }

    /**
     * Creates a pair of a Button and a command supplier function.
     *
     * @param button The button to bind.
     * @param commandSupplier A function that supplies the command to be executed when the button is pressed.
     * @return A pair of the button and the command supplier.
     */
    @JvmStatic
    fun bind(
        button: Button,
        pressedCommand: () -> Command = { InstantCommand() },
        releasedCommand: () -> Command = { InstantCommand() },
    ): ButtonBinding = Triple(button, pressedCommand, releasedCommand)

    /**
     * Periodically checks the state of each button and schedules the corresponding command if the button is pressed.
     */
    override fun periodic() {
        buttonMaps.forEach { (controller, button, pair) ->
            if (button.checkPressed(controller)) {
                pair.first().schedule()
            }
            if (button.checkReleased(controller)) {
                pair.second().schedule()
            }
        }
    }
}
