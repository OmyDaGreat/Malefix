package xyz.malefic.frc.pingu.motor.talonfx

import com.ctre.phoenix6.controls.ControlRequest
import com.ctre.phoenix6.hardware.TalonFX
import xyz.malefic.frc.pingu.motor.Mongu

/**
 * Wrapper around [TalonFX] that implements [Mongu] configuration and control using a [ControlRequest].
 *
 * Type parameter `T` represents the specific subtype of [ControlRequest] used to send control commands.
 *
 * Usage:
 * ```kotlin
 * // Provide a control request and a function that produces a new request with the desired output.
 * val velocityVoltage = VelocityVoltage()
 * val motor = TonguFX(1, velocityVoltage, { out -> this.withVelocity(out) })
 *
 * // Configure via DSL, then control the motor.
 * motor.configure {
 *   pingu.p = 0.1
 *   inverted = InvertedValue.Clockwise_Positive
 * }
 * motor.movePWM(0.5)      // simple PWM control
 * motor.control(25)       // send a ControlRequest with output 25
 * motor.stopMotor()       // stops using configured stop behavior
 * ```
 *
 * Notes:
 * - `withOutput` must return a new or modified instance of `T` that encodes the requested output.
 *   Implementations should avoid mutating a shared template in-place unless it is safe for concurrent use.
 * - The provided `controlRequest` acts as a template; the concrete request sent is created by invoking
 *   `controlRequest.withOutput(value)`.
 * - Initialization resets position to `0.0` and applies the optional `monguConfig` block.
 * - `configure` applies changes to the in-memory `TalonFXConfig` and immediately applies them to the hardware.
 *
 * @constructor Creates a TonguFX motor controller.
 * @param deviceId The CAN device id for the TalonFX.
 * @param controlRequest A template control request instance used as the base for control operations.
 * @param withOutput An extension function on the control request type that returns a new request with the given output value.
 * @param canbus Optional CAN bus name; defaults to an empty string which selects the default bus.
 * @param monguConfig Optional DSL block to configure the motor's [TalonFXConfig] during initialization.
 */
class TonguFX<T : ControlRequest>(
    deviceId: Int,
    val controlRequest: T,
    val withOutput: T.(Double) -> T,
    canbus: String = "",
    monguConfig: TalonFXConfig.() -> Unit = {},
) : TalonFX(deviceId, canbus),
    Mongu<TalonFXConfig> {
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
    override fun movePWM(value: Double) {
        set(value)
    }

    /**
     * Sets the motor control using a [ControlRequest].
     *
     * @param double The output value to set in the control request.
     */
    fun control(double: Double) {
        this.setControl(controlRequest.withOutput(double))
    }
}
