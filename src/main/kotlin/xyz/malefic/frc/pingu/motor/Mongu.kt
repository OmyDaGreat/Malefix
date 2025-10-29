package xyz.malefic.frc.pingu.motor

/**
 * Interface for motor control that defines common methods all motors must have.
 *
 * This interface provides a common abstraction for motor control, allowing different
 * motor types to be used interchangeably while providing type-safe access to their
 * specific features through implementations.
 *
 * ## Usage Examples:
 * ```kotlin
 * // Create a TalonFX motor with direct access to control classes
 * val motor = TonguFX(1)
 * motor.configure {
 *     pingu.p = 0.1
 *     inverted = InvertedValue.Clockwise_Positive
 * }
 * motor.move(0.5)  // Move using configured control
 *
 * // Direct access to control classes
 * motor.setControl(motor.positionControl.withPosition(10.0))
 * ```
 *
 * @param T The type of motor configuration used.
 */
interface Mongu<T : MonguConfig<*>> {
    /**
     * The configuration for this motor instance.
     * This property is updated whenever the [configure] method is called.
     */
    val configuration: T

    /**
     * Configures the motor using a DSL-style configuration block.
     *
     * ```kotlin
     * motor.configure {
     *    inverted = true
     *    pingu.p = 0.1
     * }
     * ```
     *
     * @param block A lambda that applies configuration settings to the motor.
     */
    fun configure(block: T.() -> Unit)

    /**
     * Moves the motor using the default control method.
     *
     * The specific behavior depends on the motor implementation and its configuration.
     * The meaning of the value depends on the control type:
     * - **PWM**: Duty cycle from -1.0 to 1.0
     * - **VOLTAGE**: Voltage in Volts
     * - **POSITION**: Target position in rotations
     * - **VELOCITY**: Target velocity in rotations per second
     *
     * @param value The control value (meaning depends on motor type and configuration).
     */
    fun movePWM(value: Double)

    /**
     * Stops the motor immediately.
     *
     * This method should halt all motor movement and bring it to a stop.
     */
    operator fun not()
}
