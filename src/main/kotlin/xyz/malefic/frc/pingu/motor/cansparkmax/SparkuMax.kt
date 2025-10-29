package xyz.malefic.frc.pingu.motor.cansparkmax

import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax
import xyz.malefic.frc.pingu.motor.Mongu

/**
 * PWMSparkMax motor implementation that extends [PWMSparkMax] and implements the [Mongu] interface.
 *
 * This class provides direct access to PWMSparkMax-specific functionality while implementing
 * the common Mongu interface. It provides PWM control for the motor.
 *
 * ## Usage Examples:
 * ```kotlin
 * // Create a PWMSparkMax motor
 * val motor = SparkuMax(0)
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
class SparkuMax(
    channel: Int,
    monguConfig: PWMSparkMaxConfig.() -> Unit = {},
) : PWMSparkMax(channel),
    Mongu<PWMSparkMaxConfig> {
    /**
     * The configuration for this PWMSparkMax motor.
     */
    override var configuration: PWMSparkMaxConfig = PWMSparkMaxConfig()
        private set

    /**
     * Property to calculate the PWM voltage ratio for [SparkuMax].
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
    override fun configure(block: PWMSparkMaxConfig.() -> Unit) {
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
    override operator fun not() = stopMotor()
}
