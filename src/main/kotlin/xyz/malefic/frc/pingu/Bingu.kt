package xyz.malefic.frc.pingu

import edu.wpi.first.wpilibj.XboxController
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.SubsystemBase
import xyz.malefic.frc.emu.Button
import xyz.malefic.frc.extension.Kommand.cmd

/**
 * Bingu provides a centralized, declarative system for binding Xbox controller buttons to WPILib commands.
 *
 * This object is designed to simplify the process of mapping controller inputs to robot actions, supporting both
 * simple and complex command scheduling. It supports two binding styles:
 * - Deprecated vararg-based binding for legacy code.
 * - Modern DSL-based binding for clarity, flexibility, and maintainability.
 *
 * Usage:
 * 1. Use the DSL-based `bindings` extension for new code:
 *    ```kotlin
 *    controller.bindings {
 *        press(Button.A) { MyCommand() }
 *        release(Button.A) { MyReleaseCommand() }
 *    }
 *    ```
 * 2. The deprecated vararg-based method is supported for backward compatibility:
 *    ```kotlin
 *    controller.bindings(
 *        Bingu.bind(Button.A, { MyCommand() }, { MyReleaseCommand() })
 *    )
 *    ```
 *
 * Internally, Bingu tracks all bindings and schedules commands in its periodic loop. Each button can have a press and release command.
 * If a button is not explicitly bound, it defaults to an InstantCommand (does nothing).
 *
 * See the documentation for each function for more details and examples.
 */
object Bingu : SubsystemBase() {
    /**
     * Internal list storing all controller-button-command bindings.
     *
     * Each entry is a ControllerButtonBinding, which pairs an XboxController with a ButtonBinding (button, press command, release command).
     * This list is used by the periodic loop to check button states and schedule commands.
     *
     * You should not modify this list directly; use the provided binding functions.
     */
    private val buttonMaps: MutableList<ControllerButtonBinding> = mutableListOf()

    /**
     * Extension function for XboxController to bind multiple button-command pairs (deprecated).
     *
     * This method is retained for legacy code. Prefer the DSL-based `bindings` extension for new code.
     *
     * @param pair Vararg of ButtonBinding, each specifying a button and its press/release commands.
     *
     * Example:
     * ```kotlin
     * controller.bindings(
     *     Bingu.bind(Button.A, { MyCommand() }, { MyReleaseCommand() })
     * )
     * ```
     */
    @JvmStatic
    @SafeVarargs
    @Deprecated("Use the dsl-based bind function instead for better clarity and flexibility.")
    fun XboxController.bindings(vararg pair: ButtonBinding) =
        pair.forEach { (button, pressedCommand, releasedCommand) ->
            buttonMaps.add(ControllerButtonBinding(this, Triple(button, pressedCommand, releasedCommand)))
        }

    /**
     * Extension function for XboxController to bind multiple button-command pairs using a DSL builder.
     *
     * This is the recommended way to configure button bindings. The builder allows you to specify press and release
     * commands for each button in a clear, declarative style.
     *
     * @param builder Lambda with receiver for configuring button bindings. Use ButtonBindingsBuilder to specify press and release commands for each button.
     *
     * Example:
     * ```kotlin
     * controller.bindings {
     *     press(Button.A) { MyCommand() }
     *     release(Button.A) { MyReleaseCommand() }
     *     press(Button.B) { println("Pressed B!") }
     * }
     * ```
     */
    @JvmStatic
    fun XboxController.bindings(builder: ButtonBindingsBuilder.() -> Unit) {
        val bindings = ButtonBindingsBuilder().apply(builder).build()
        bindings.forEach { (button, pressedCommand, releasedCommand) ->
            buttonMaps.add(ControllerButtonBinding(this, Triple(button, pressedCommand, releasedCommand)))
        }
    }

    /**
     * Creates a ButtonBinding for a button and its press/release command suppliers (deprecated).
     *
     * This function is retained for legacy code. Prefer the DSL-based builder for new code.
     *
     * @param button The button to bind.
     * @param pressedCommand The command supplier for the press event (defaults to InstantCommand).
     * @param releasedCommand The command supplier for the release event (defaults to InstantCommand).
     * @return A ButtonBinding (Triple) representing the binding.
     *
     * Example:
     * ```kotlin
     * Bingu.bind(Button.A, { MyCommand() }, { MyReleaseCommand() })
     * ```
     */
    @JvmStatic
    @Deprecated("Use the dsl-based bind function instead for better clarity and flexibility.")
    fun bind(
        button: Button,
        pressedCommand: () -> Command = { InstantCommand() },
        releasedCommand: () -> Command = { InstantCommand() },
    ): ButtonBinding = Triple(button, pressedCommand, releasedCommand)

    /**
     * Periodically checks the state of each button and schedules the corresponding command if the button is pressed or released.
     *
     * This function is called automatically by WPILib. It iterates through all bindings and:
     * - Schedules the press command if the button is pressed.
     * - Schedules the release command if the button is released.
     *
     * You should not call this function directly.
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
 * Type alias for a controller-button binding.
 *
 * Represents a pairing of an XboxController and a ButtonBinding (button, press command, release command).
 * Used internally by Bingu to track all configured bindings.
 */
typealias ControllerButtonBinding = Pair<XboxController, ButtonBinding>

/**
 * Type alias for a button binding.
 *
 * Represents a Triple of Button, press command supplier, and release command supplier.
 * Used to specify the actions to take when a button is pressed or released.
 *
 * Example:
 * ```kotlin
 * Triple(Button.A, { MyCommand() }, { MyReleaseCommand() })
 * ```
 */
typealias ButtonBinding = Triple<Button, () -> Command, () -> Command>

/**
 * Builder class for configuring button bindings with press and release commands.
 */
class ButtonBindingsBuilder {
    private val bindings = mutableMapOf<Button, Pair<() -> Command, () -> Command>>()

    /**
     * Sets the command supplier to be executed when the specified button is pressed.
     *
     * This overload accepts a supplier of a Command. If the button was previously bound,
     * only the press command is updated; the release command remains unchanged.
     *
     * @param button The button to bind.
     * @param command The command supplier for the press event.
     *
     * Example:
     * ```kotlin
     * press(Button.A) { MyCommand() }
     * ```
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
     * This overload accepts a supplier of a Command. If the button was previously bound,
     * only the release command is updated; the press command remains unchanged.
     *
     * @param button The button to bind.
     * @param command The command supplier for the release event.
     *
     * Example:
     * ```kotlin
     * release(Button.A) { MyReleaseCommand() }
     * ```
     */
    fun release(
        button: Button,
        command: () -> Command,
    ) {
        val (pressed, _) = bindings[button] ?: ({ InstantCommand() } to { InstantCommand() })
        bindings[button] = pressed to command
    }

    /**
     * Sets the command supplier to be executed when the specified button is released.
     *
     * This overload accepts a supplier of a Command. If the button was previously bound,
     * only the release command is updated; the press command remains unchanged.
     *
     * @param button The button to bind.
     * @param command The command supplier for the release event.
     *
     * Example:
     * ```kotlin
     * unpress(Button.A) { MyReleaseCommand() }
     * ```
     * Overload just for Jayden Sun
     */
    @Suppress("kotlin:S4144")
    fun unpress(
        button: Button,
        command: () -> Command,
    ) {
        val (pressed, _) = bindings[button] ?: ({ InstantCommand() } to { InstantCommand() })
        bindings[button] = pressed to command
    }

    /**
     * Sets the command supplier to be executed when the specified button is pressed.
     *
     * This overload accepts a lambda of type `() -> Unit`, which is wrapped in an InstantCommand.
     * Useful for simple actions that do not require a full Command implementation.
     *
     * @param button The button to bind.
     * @param command The lambda to execute for the press event.
     *
     * Example:
     * ```kotlin
     * press(Button.B) { println("Pressed B!") }
     * ```
     */
    fun press(
        button: Button,
        command: () -> Unit,
    ) {
        val (_, released) = bindings[button] ?: ({ InstantCommand() } to { InstantCommand() })
        bindings[button] = { cmd { command() } } to released
    }

    /**
     * Sets the command supplier to be executed when the specified button is released.
     *
     * This overload accepts a lambda of type `() -> Unit`, which is wrapped in an InstantCommand.
     * Useful for simple actions that do not require a full Command implementation.
     *
     * @param button The button to bind.
     * @param command The lambda to execute for the release event.
     *
     * Example:
     * ```kotlin
     * release(Button.B) { println("Released B!") }
     * ```
     */
    fun release(
        button: Button,
        command: () -> Unit,
    ) {
        val (pressed, _) = bindings[button] ?: ({ InstantCommand() } to { InstantCommand() })
        bindings[button] = pressed to { cmd { command() } }
    }

    /**
     * Sets the command supplier to be executed when the specified button is released.
     *
     * This overload accepts a lambda of type `() -> Unit`, which is wrapped in an InstantCommand.
     * Useful for simple actions that do not require a full Command implementation.
     *
     * @param button The button to bind.
     * @param command The lambda to execute for the release event.
     *
     * Example:
     * ```kotlin
     * unpress(Button.B) { println("Released B!") }
     * ```
     * Overload just for Jayden Sun
     */
    @Suppress("kotlin:S4144")
    fun unpress(
        button: Button,
        command: () -> Unit,
    ) {
        val (pressed, _) = bindings[button] ?: ({ InstantCommand() } to { InstantCommand() })
        bindings[button] = pressed to { cmd { command() } }
    }

    /**
     * Builds and returns a list of button bindings.
     *
     * @return List of ButtonBinding objects representing the configured bindings.
     */
    internal fun build(): List<ButtonBinding> = bindings.map { (button, commands) -> Triple(button, commands.first, commands.second) }
}
