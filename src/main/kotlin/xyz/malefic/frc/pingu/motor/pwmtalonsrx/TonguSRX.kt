package xyz.malefic.frc.pingu.motor.pwmtalonsrx

import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX
import xyz.malefic.frc.pingu.motor.Mongu

/**
 * PWMTalonSRX motor implementation that extends [PWMTalonSRX] and implements the [Mongu] interface.
 *
 * This class provides direct access to PWMTalonSRX-specific functionality while implementing
 * the common Mongu interface. It provides PWM control for the motor.
 *
 * ## Usage Examples:
 * ```kotlin
 * // Create a PWMTalonSRX motor
 * val motor = TalonuSRX(0)
 * motor.configure {
 *     inverted = true
 *     deadbandElimination = false
 * }
 * motor.move(0.5)  // PWM control
 * ```
 *
 * @param channel The PWM channel for this motor controller.
 * @param monguConfig A lambda that applies initial configuration settings to the motor.
 */
class TonguSRX(
    channel: Int,
    monguConfig: PWMTalonSRXConfig.() -> Unit = {},
) : PWMTalonSRX(channel),
    Mongu<PWMTalonSRXConfig> {
    /**
     * The configuration for this PWMTalonSRX motor.
     */
    override var configuration: PWMTalonSRXConfig = PWMTalonSRXConfig()
        private set

    /**
     * Property to calculate the PWM voltage ratio for [TonguSRX].
     *
     * @return The ratio of the motor voltage to the battery voltage.
     */
    val pwm
        get() = get()

    init {
        configure(monguConfig)
    }

    /**
     * Configures the motor using a DSL-style configuration block.
     *
     * ```kotlin
     * motor.configure {
     *    inverted = true
     *    deadbandElimination = false
     * }
     * ```
     *
     * @param block A lambda that applies configuration settings to the motor.
     */
    override fun configure(block: PWMTalonSRXConfig.() -> Unit) {
        configuration.apply(block)
        configuration.applyTo(this)
    }

    /**
     * Moves the motor using PWM control.
     *
     * Sets the motor output to the specified duty cycle value.
     *
     * @param value The duty cycle from -1.0 to 1.0.
     */
    override fun movePWM(value: Double) {
        set(value)
    }

    /**
     * Stops the motor when the logical not operator (`!`) is applied to this instance.
     *
     * Example:
     * ```kotlin
     * !motor  // calls stopMotor()
     * ```
     */
    override fun not() = stopMotor()
}
