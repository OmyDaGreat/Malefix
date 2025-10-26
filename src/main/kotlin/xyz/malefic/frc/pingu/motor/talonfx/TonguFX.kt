package xyz.malefic.frc.pingu.motor.talonfx

import com.ctre.phoenix6.controls.PositionVoltage
import com.ctre.phoenix6.controls.VelocityVoltage
import com.ctre.phoenix6.hardware.TalonFX
import xyz.malefic.frc.pingu.motor.Mongu

/**
 * TalonFX motor implementation that extends [TalonFX] and implements the [Mongu] interface.
 *
 * This class provides direct access to TalonFX-specific functionality while implementing
 * the common Mongu interface. It provides direct access to control classes for position,
 * PWM, and velocity control.
 *
 * ## Usage Examples:
 * ```kotlin
 * // Create a TalonFX motor
 * val motor = TonguFX(1)
 * motor.configure {
 *     pingu.p = 0.1
 *     inverted = InvertedValue.Clockwise_Positive
 * }
 * motor.move(0.5)  // PWM control by default
 *
 * // Direct access to control classes
 * motor.setControl(motor.positionControl.withPosition(10.0))
 * motor.setControl(motor.velocityControl.withVelocity(100.0))
 * ```
 *
 * @param deviceId The CAN ID of the TalonFX motor.
 * @param canbus The CAN bus name (defaults to empty string for default bus).
 * @param monguConfig A lambda that applies initial configuration settings to the motor.
 */
class TonguFX(
    deviceId: Int,
    canbus: String = "",
    monguConfig: TalonFXConfig.() -> Unit = {},
) : TalonFX(deviceId, canbus),
    Mongu<TalonFXConfig> {
    /**
     * Pre-allocated [PositionVoltage] control object for position control mode.
     * Use this to directly set position control with additional parameters.
     */
    val positionControl: PositionVoltage = PositionVoltage(0.0)

    /**
     * Pre-allocated [VelocityVoltage] control object for velocity control mode.
     * Use this to directly set velocity control with additional parameters.
     */
    val velocityControl: VelocityVoltage = VelocityVoltage(0.0)

    /**
     * The configuration for this TalonFX motor.
     */
    override var configuration: TalonFXConfig = TalonFXConfig()
        private set

    init {
        setPosition(0.0)
        configure(monguConfig)
    }

    /**
     * Configures the motor using a DSL-style configuration block.
     *
     * ```kotlin
     * motor.configure {
     *    pingu.p = 0.1
     *    inverted = InvertedValue.Clockwise_Positive
     *    currentLimits = 40.0 to 60.0
     * }
     * ```
     *
     * @param block A lambda that applies configuration settings to the motor.
     */
    override fun configure(block: TalonFXConfig.() -> Unit) {
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
    override fun move(value: Double) {
        set(value)
    }

    /**
     * Stops the motor by invoking the configured stop function.
     */
    override fun stopMotor() {
        configuration.stop(this)
    }
}
