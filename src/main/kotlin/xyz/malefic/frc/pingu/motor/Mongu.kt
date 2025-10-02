package xyz.malefic.frc.pingu.motor

import com.ctre.phoenix6.hardware.TalonFX
import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX
import xyz.malefic.frc.pingu.motor.control.MonguControlPWM
import xyz.malefic.frc.pingu.motor.control.MonguControlPosition
import xyz.malefic.frc.pingu.motor.control.MonguControlVoltage
import xyz.malefic.frc.pingu.motor.control.PWM
import xyz.malefic.frc.pingu.motor.control.Position
import xyz.malefic.frc.pingu.motor.control.Voltage
import xyz.malefic.frc.pingu.motor.pwmtalonsrx.PWMTalonSRXConfig
import xyz.malefic.frc.pingu.motor.talonfx.TalonFXConfig

/**
 * A generic motor wrapper class that allows configuration of different motor types.
 *
 * Make sure to configure the motor after instantiation to apply desired settings.
 *
 * @param T The type of motor being wrapped.
 * @property motor The motor instance being wrapped.
 */
class Mongu<T : Any>(
    val motor: T,
    block: MonguConfig<out T>.() -> Unit = {},
) {
    /**
     * Holds the last configuration applied to this motor.
     * This property is updated whenever the [configure] method is called.
     * It allows retrieval of the configuration settings for inspection or reuse.
     */
    lateinit var configuration: MonguConfig<T>

    init {
        require(motor is TalonFX || motor is PWMTalonSRX) { "Unsupported motor type" }
        configure(block)
    }

    /**
     * Configures the motor using a DSL-style configuration block.
     *
     * @param block A lambda that applies configuration settings to the motor.
     * @throws IllegalArgumentException If the motor type is unsupported.
     */
    @Suppress("UNCHECKED_CAST")
    fun configure(block: MonguConfig<out T>.() -> Unit = {}) {
        val config =
            when (motor) {
                is TalonFX -> TalonFXConfig().apply(block as MonguConfig<TalonFX>.() -> Unit)
                is PWMTalonSRX -> PWMTalonSRXConfig().apply(block as MonguConfig<PWMTalonSRX>.() -> Unit)
                else -> throw IllegalArgumentException("Unsupported motor type")
            } as MonguConfig<T>
        config.applyTo(motor)
        configuration = config
    }

    /**
     * Sets the control value for the motor using the specified control type.
     *
     * This function determines the appropriate control function based on the reified type parameter [A],
     * and applies the provided [value] to the motor. Supported control types include [PWM], [Voltage], and [Position].
     *
     * ## Usage Examples:
     *
     * ### PWM Control (Duty Cycle: -1.0 to 1.0)
     * ```kotlin
     * val motor = Mongu(TalonFX(1))
     * motor.set(0.5.pwm)     // 50% forward
     * motor.set(-0.3.pwm)    // 30% reverse
     * motor.set(0.0.pwm)     // stop
     * ```
     *
     * ### Voltage Control (in Volts)
     * ```kotlin
     * val motor = Mongu(TalonFX(1))
     * motor.set(12.0.voltage)  // 12V forward
     * motor.set(-6.0.voltage)  // 6V reverse
     * motor.set(0.0.voltage)   // stop
     * ```
     *
     * ### Position Control (in rotations or encoder units)
     * ```kotlin
     * val motor = Mongu(TalonFX(1)) {
     *     pingu = Pingu(p = 0.1, i = 0.0, d = 0.01)  // PID required for position control
     * }
     * motor.set(10.0.position)   // move to position 10
     * motor.set(-5.5.position)   // move to position -5.5
     * ```
     *
     * ## Motor Support Matrix:
     * - **TalonFX**: Supports PWM, Voltage, and Position control
     * - **PWMTalonSRX**: Supports PWM control only (voltage and position will throw UnsupportedOperationException)
     *
     * @param A The type of control to apply (e.g., PWM, Voltage, Position).
     * @param value The control value to set, wrapped in a [MonguControl] of the corresponding type.
     *
     * @throws IllegalArgumentException If the control type is unsupported or the value is invalid.
     * @throws UnsupportedOperationException If the control type is not supported for the current motor type.
     */
    inline fun <reified A> set(value: MonguControl<A>) {
        val controlFunction: ((T, Double) -> Unit)? =
            when (A::class) {
                PWM::class -> configuration.pwmControl
                Voltage::class -> configuration.voltageControl
                Position::class -> configuration.positionControl
                else -> throw IllegalArgumentException("Unsupported control type: ${A::class}")
            }

        controlFunction?.invoke(
            motor,
            (value as? MonguControlPWM)?.value
                ?: (value as? MonguControlVoltage)?.value
                ?: (value as? MonguControlPosition)?.value
                ?: throw IllegalArgumentException("Invalid control value"),
        )
            ?: throw UnsupportedOperationException(
                "Control type ${A::class.simpleName} is not supported for motor type ${motor::class.simpleName}",
            )
    }
}
