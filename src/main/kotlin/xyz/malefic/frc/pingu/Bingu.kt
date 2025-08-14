package xyz.malefic.frc.pingu

import edu.wpi.first.wpilibj.XboxController
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.SubsystemBase
import xyz.malefic.frc.emu.Button

/**
 * Type alias for a Triple consisting of an XboxController, a Button, and a command supplier function.
 */
typealias ControllerButtonBinding = Pair<XboxController, ButtonBinding>

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
    @Deprecated("Use the dsl-based bind function instead for better clarity and flexibility.")
    fun XboxController.bindings(vararg pair: ButtonBinding) =
        pair.forEach { (button, pressedCommand, releasedCommand) ->
            buttonMaps.add(ControllerButtonBinding(this, Triple(button, pressedCommand, releasedCommand)))
        }

    /**
     * Binds multiple button-command pairs to an XboxController using a DSL builder.
     *
     * @param builder Lambda with receiver for configuring button bindings.
     *                Use ButtonBindingsBuilder to specify press and release commands for each button.
     */
    @JvmStatic
    fun XboxController.bindings(builder: ButtonBindingsBuilder.() -> Unit) {
        val bindings = ButtonBindingsBuilder().apply(builder).build()
        bindings.forEach { (button, pressedCommand, releasedCommand) ->
            buttonMaps.add(ControllerButtonBinding(this, Triple(button, pressedCommand, releasedCommand)))
        }
    }

    /**
     * Creates a pair of a Button and a command supplier function.
     *
     * @param button The button to bind.
     * @param pressedCommand The command supplier to execute when the button is pressed.
     * @param releasedCommand The command supplier to execute when the button is released.
     * @return A pair of the button and the command supplier.
     */
    @JvmStatic
    @Deprecated("Use the dsl-based bind function instead for better clarity and flexibility.")
    fun bind(
        button: Button,
        pressedCommand: () -> Command = { InstantCommand() },
        releasedCommand: () -> Command = { InstantCommand() },
    ): ButtonBinding = Triple(button, pressedCommand, releasedCommand)

    /**
     * Periodically checks the state of each button and schedules the corresponding command if the button is pressed.
     */
    override fun periodic() {
        buttonMaps.forEach { (controller, triple) ->
            val (button, press, release) = triple
            if (triple.first.checkPressed(controller)) {
                press().schedule()
            }
            if (button.checkReleased(controller)) {
                release().schedule()
            }
        }
    }
}

/**
 * Builder class for configuring button bindings with press and release commands.
 */
class ButtonBindingsBuilder {
    /**
     * Stores bindings for each button, mapping to a pair of press and release command suppliers.
     */
    private val bindings = mutableMapOf<Button, Pair<() -> Command, () -> Command>>()

    /**
     * Sets the command supplier to be executed when the specified button is pressed.
     *
     * @param button The button to bind.
     * @param command The command supplier for the press event.
     */
    fun press(
        button: Button,
        command: () -> Command,
    ) {
        val (_, released) = bindings[button] ?: ({ InstantCommand() } to { InstantCommand() })
        bindings[button] = command to released
    }

    /**
     * Sets the command supplier to be executed when the specified button is released.
     *
     * @param button The button to bind.
     * @param command The command supplier for the release event.
     */
    fun release(
        button: Button,
        command: () -> Command,
    ) {
        val (pressed, _) = bindings[button] ?: ({ InstantCommand() } to { InstantCommand() })
        bindings[button] = pressed to command
    }

    /**
     * Builds and returns a list of button bindings.
     *
     * @return List of ButtonBinding objects representing the configured bindings.
     */
    internal fun build(): List<ButtonBinding> = bindings.map { (button, commands) -> Triple(button, commands.first, commands.second) }
}
