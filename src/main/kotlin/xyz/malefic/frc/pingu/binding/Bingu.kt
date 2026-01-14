package xyz.malefic.frc.pingu.binding

import edu.wpi.first.wpilibj.XboxController
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.InstantCommand
import edu.wpi.first.wpilibj2.command.SubsystemBase
import xyz.malefic.frc.emu.Button
import xyz.malefic.frc.extension.Kommand.cmd
import xyz.malefic.frc.extension.invoke
import xyz.malefic.frc.state.IntentCommand

/**
 * [Bingu] provides a centralized, declarative system for binding [XboxController] buttons to WPILib [Command]s or [IntentCommand]s.
 *
 * This object is designed to simplify the process of mapping controller inputs to robot actions, supporting both
 * simple and complex command scheduling. It contains a modern DSL-based binding for clarity, flexibility, and maintainability.
 *
 * Usage:
 * * DSL-based [bindings] extension:
 *    ```kotlin
 *    controller.bindings {
 *      press(Button.A, MyCommand())
 *      release(Button.A, MyReleaseCommand())
 *      press(Button.B) { println("Pressed B!") }
 *      hold(Button.B) { println("Holding B!") }
 *    }
 *    ```
 *
 * Internally, [Bingu] tracks all bindings and schedules commands in its [periodic] loop. Each button can have a press, release, and hold command.
 * If a button is not explicitly bound, it defaults to an [InstantCommand] (does nothing).
 *
 * See the documentation for each function for more details and examples.
 */
object Bingu : SubsystemBase() {
    /**
     * Internal list storing all controller-button-command bindings.
     *
     * Each entry is a [ControllerButtonBinding], which pairs an [XboxController] with a [ButtonBinding] (button, press command, release command, hold command).
     * This list is used by the [periodic] loop to check button states and schedule commands.
     *
     * You should not modify this list directly; use the provided binding functions.
     */
    private val buttonMaps: MutableList<ControllerButtonBinding> = mutableListOf()

    /**
     * Stores the previous pressed state of each button for each controller.
     *
     * The key is a [Pair] of [XboxController] and [Button], and the value is a [Boolean] indicating
     * whether the button was pressed in the previous periodic cycle.
     * Used to detect button press and release events.
     */
    private val previousButtonStates: MutableMap<Pair<XboxController, Button>, Boolean> = mutableMapOf()

    /**
     * Extension function for [XboxController] to bind multiple button-command pairs using a DSL builder.
     *
     * This is the recommended way to configure button bindings. The builder allows you to specify press and release
     * commands for each button in a clear, declarative style.
     *
     * @param builder Lambda with receiver for configuring button bindings. Use [ButtonBindingsBuilder] to specify press, release, and hold commands for each button.
     *
     * Example:
     * ```kotlin
     * controller.bindings {
     *     press(Button.A, MyCommand())
     *     release(Button.A, MyReleaseCommand())
     *     press(Button.B) { println("Pressed B!") }
     *     hold(Button.B) { println("Holding B!") }
     * }
     * ```
     */
    @JvmStatic
    fun XboxController.bindings(builder: ButtonBindingsBuilder.() -> Unit) {
        val bindings = ButtonBindingsBuilder().apply(builder).build()
        bindings.forEach { (button, pressedCommand, releasedCommand, heldCommand) ->
            buttonMaps.add(ControllerButtonBinding(this, Quadruple(button, pressedCommand, releasedCommand, heldCommand)))
        }
    }

    /**
     * Periodically checks the state of each button and schedules the corresponding command if the button is pressed or released.
     *
     * This function is called automatically by WPILib. It iterates through all bindings and:
     * - Schedules the press command if the button is pressed.
     * - Schedules the release command if the button is released.
     * - Schedules the hold command if the button is currently pressed.
     *
     * You should not call this function directly.
     */
    override fun periodic() {
        for ((controller, quadruple) in buttonMaps) {
            val (button, press, release, hold) = quadruple
            val key = controller to button
            val isPressed = button.checkPressed(controller)
            val wasPressed = previousButtonStates.getOrDefault(key, false)

            if (isPressed != wasPressed) {
                if (isPressed) press()() else release()()
            }
            if (isPressed) hold()()

            previousButtonStates[key] = isPressed
        }
    }
}

/**
 * Type alias for a controller-button binding.
 *
 * Represents a pairing of an [XboxController] and a [ButtonBinding] (button, press command, release command, hold command).
 * Used internally by [Bingu] to track all configured bindings.
 */
typealias ControllerButtonBinding = Pair<XboxController, ButtonBinding>

/**
 * Type alias for a button binding.
 *
 * Represents a [Quadruple] of [Button], press command supplier, release command supplier, and hold command supplier.
 * Used to specify the actions to take when a button is pressed, released, or held.
 *
 * Example:
 * ```kotlin
 * Quadruple(Button.A, { MyCommand() }, { MyReleaseCommand() }, { MyHoldCommand() })
 * ```
 */
typealias ButtonBinding = Quadruple<Button, () -> Command, () -> Command, () -> Command>

/**
 * Builder class for configuring button bindings with press and release commands.
 */
class ButtonBindingsBuilder {
    private val bindings = mutableMapOf<Button, Triple<() -> Command, () -> Command, () -> Command>>()

    /**
     * Sets the command supplier to be executed when the specified [Button] is pressed.
     *
     * This overload accepts a supplier of a [Command]. If the button was previously bound,
     * only the press command is updated; the release command remains unchanged.
     *
     * @param button The [Button] to bind.
     * @param command The [Command] supplier for the press event.
     *
     * Example:
     * ```kotlin
     * press(Button.A, MyCommand())
     * ```
     */
    fun press(
        button: Button,
        command: Command,
    ) {
        val (_, released, held) = bindings[button] ?: Triple({ InstantCommand() }, { InstantCommand() }, { InstantCommand() })
        bindings[button] = Triple({ command }, released, held)
    }

    /**
     * Sets the command supplier to be executed when the specified button is released.
     *
     * This overload accepts a supplier of a [Command]. If the button was previously bound,
     * only the release command is updated; the press command remains unchanged.
     *
     * @param button The button to bind.
     * @param command The command supplier for the release event.
     *
     * Example:
     * ```kotlin
     * release(Button.A, MyReleaseCommand())
     * ```
     */
    fun release(
        button: Button,
        command: Command,
    ) {
        val (pressed, _, held) = bindings[button] ?: Triple({ InstantCommand() }, { InstantCommand() }, { InstantCommand() })
        bindings[button] = Triple(pressed, { command }, held)
    }

    /**
     * Sets the command supplier to be executed when the specified button is released.
     *
     * This overload accepts a supplier of a [Command]. If the button was previously bound,
     * only the release command is updated; the press command remains unchanged.
     *
     * @param button The button to bind.
     * @param command The command supplier for the release event.
     *
     * Example:
     * ```kotlin
     * unpress(Button.A, MyReleaseCommand())
     * ```
     * Overload just for Jayden Sun
     */
    fun unpress(
        button: Button,
        command: Command,
    ) {
        val (pressed, _, held) = bindings[button] ?: Triple({ InstantCommand() }, { InstantCommand() }, { InstantCommand() })
        bindings[button] = Triple(pressed, { command }, held)
    }

    /**
     * Sets the command supplier to be executed when the specified button is held.
     *
     * This overload accepts a supplier of a [Command]. If the button was previously bound,
     * only the hold command is updated; the press and release commands remain unchanged.
     *
     * @param button The button to bind.
     * @param command The command supplier for the hold event.
     *
     * Example:
     * ```kotlin
     * hold(Button.A, MyHoldCommand())
     * ```
     */
    fun hold(
        button: Button,
        command: Command,
    ) {
        val (pressed, released, _) = bindings[button] ?: Triple({ InstantCommand() }, { InstantCommand() }, { InstantCommand() })
        bindings[button] = Triple(pressed, released) { command }
    }

    /**
     * Sets the command supplier to be executed when the specified button is pressed.
     *
     * This overload accepts a lambda of type `() -> Unit`, which is wrapped in an [InstantCommand].
     * Useful for simple actions that do not require a full [Command] implementation.
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
        val (_, released, held) = bindings[button] ?: Triple({ InstantCommand() }, { InstantCommand() }, { InstantCommand() })
        bindings[button] = Triple({ cmd { command() } }, released, held)
    }

    /**
     * Sets the command supplier to be executed when the specified button is released.
     *
     * This overload accepts a lambda of type `() -> Unit`, which is wrapped in an [InstantCommand].
     * Useful for simple actions that do not require a full [Command] implementation.
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
        val (pressed, _, held) = bindings[button] ?: Triple({ InstantCommand() }, { InstantCommand() }, { InstantCommand() })
        bindings[button] = Triple(pressed, { cmd { command() } }, held)
    }

    /**
     * Sets the command supplier to be executed when the specified button is released.
     *
     * This overload accepts a lambda of type `() -> Unit`, which is wrapped in an [InstantCommand].
     * Useful for simple actions that do not require a full [Command] implementation.
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
    fun unpress(
        button: Button,
        command: () -> Unit,
    ) {
        val (pressed, _, held) = bindings[button] ?: Triple({ InstantCommand() }, { InstantCommand() }, { InstantCommand() })
        bindings[button] = Triple(pressed, { cmd { command() } }, held)
    }

    /**
     * Sets the command supplier to be executed when the specified button is held.
     *
     * This overload accepts a lambda of type `() -> Unit`, which is wrapped in an [InstantCommand].
     * Useful for simple actions that do not require a full [Command] implementation.
     *
     * @param button The button to bind.
     * @param command The lambda to execute for the hold event.
     *
     * Example:
     * ```kotlin
     * hold(Button.B) { println("Holding B!") }
     * ```
     */
    fun hold(
        button: Button,
        command: () -> Unit,
    ) {
        val (pressed, released, _) = bindings[button] ?: Triple({ InstantCommand() }, { InstantCommand() }, { InstantCommand() })
        bindings[button] = Triple(pressed, released) { cmd { command() } }
    }

    /**
     * Builds and returns a list of button bindings.
     *
     * @return List of [ButtonBinding] objects representing the configured bindings.
     */
    internal fun build(): List<ButtonBinding> =
        bindings.map { (button, commands) ->
            Quadruple(button, commands.first, commands.second, commands.third)
        }
}

/**
 * Helper data class for quadruple
 */
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
