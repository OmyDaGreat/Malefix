package xyz.malefic.frc.pingu.motor

import com.ctre.phoenix6.configs.CANcoderConfiguration
import com.ctre.phoenix6.hardware.TalonFX
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax
import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX
import xyz.malefic.frc.pingu.encoder.Engu
import xyz.malefic.frc.pingu.motor.ControlType.POSITION
import xyz.malefic.frc.pingu.motor.ControlType.PWM
import xyz.malefic.frc.pingu.motor.ControlType.VELOCITY
import xyz.malefic.frc.pingu.motor.ControlType.VOLTAGE
import xyz.malefic.frc.pingu.motor.cansparkmax.PWMSparkMaxConfig
import xyz.malefic.frc.pingu.motor.pwmtalonsrx.PWMTalonSRXConfig
import xyz.malefic.frc.pingu.motor.talonfx.TalonFXConfig

/**
 * A generic motor wrapper class that allows configuration of different motor types.
 *
 * The control type is locked at initialization and cannot be changed during runtime.
 * This ensures type safety and prevents accidental control type switching.
 *
 * ## Usage Examples:
 * ```kotlin
 * // Create a motor with PWM control
 * val motor = Mongu(TalonFX(1), control = ControlType.PWM)
 * motor.move(0.5)  // 50% forward
 *
 * // Create a motor with position control
 * val posMotor = Mongu(TalonFX(2), control = ControlType.POSITION) {
 *     this as TalonFXConfig
 *     pingu.p = 0.1
 * }
 * posMotor.move(10.0)  // move to position 10
 * ```
 *
 * @param T The type of motor being wrapped.
 * @property motor The motor instance being wrapped.
 * @property engu An optional encoder associated with the motor.
 * @property control The control type that this motor is locked to.
 * @param monguConfig A lambda that applies configuration settings to the motor.
 * @param enguConfig A lambda that applies configuration settings to the encoder.
 */
class Mongu<T : Any>(
    val motor: T,
    @Suppress("CanBeParameter", "RedundantSuppression") val engu: Engu? = null,
    val control: ControlType,
    enguConfig: CANcoderConfiguration.() -> Unit = {},
    monguConfig: MonguConfig<out T>.() -> Unit = {},
) {
    /**
     * Holds the last configuration applied to this motor.
     * This property is updated whenever the [configure] method is called.
     * It allows retrieval of the configuration settings for inspection or reuse.
     */
    lateinit var configuration: MonguConfig<T>

    /**
     * The compatibility checker for this motor type.
     */
    private val compatibility: MotorCompatibility = MotorCompatibility.forMotor(motor::class)

    init {
        require(motor is TalonFX || motor is PWMTalonSRX || motor is PWMSparkMax) {
            "Unsupported motor type: ${motor::class.simpleName}"
        }

        // Validate that the locked control type is compatible with the motor
        require(compatibility.supports(control)) {
            "Control type $control is not supported by motor type ${motor::class.simpleName}. " +
                "Supported types: ${compatibility.supportedTypes()}"
        }

        configure(monguConfig)
        engu?.apply { configure(enguConfig) }
    }

    /**
     * Configures the motor using a DSL-style configuration block.
     *
     * ```kotlin
     * mongu.configure {
     *    this as PWMTalonSRXConfig  // Cast to specific config type
     *    inverted = true
     *    deadbandElimination = false
     * }
     * ```
     *
     * @param block A lambda that applies configuration settings to the motor.
     * @throws IllegalArgumentException If the motor type is unsupported.
     */
    @Suppress("UNCHECKED_CAST")
    fun configure(block: MonguConfig<out T>.() -> Unit = {}) {
        val config =
            if (!::configuration.isInitialized) {
                when (motor) {
                    is TalonFX -> TalonFXConfig().apply(block as MonguConfig<TalonFX>.() -> Unit)
                    is PWMTalonSRX -> PWMTalonSRXConfig().apply(block as MonguConfig<PWMTalonSRX>.() -> Unit)
                    is PWMSparkMax -> PWMSparkMaxConfig().apply(block as MonguConfig<PWMSparkMax>.() -> Unit)
                    else -> throw IllegalArgumentException("Unsupported motor type")
                } as MonguConfig<T>
            } else {
                configuration.apply(block as MonguConfig<T>.() -> Unit)
            }
        config.applyTo(motor)
        configuration = config
    }

    /**
     * Moves the motor using the locked control type.
     *
     * The control type was specified during initialization and cannot be changed.
     * The meaning of the value depends on the locked control type:
     * - **PWM**: Duty cycle from -1.0 to 1.0
     * - **VOLTAGE**: Voltage in Volts
     * - **POSITION**: Target position in rotations
     * - **VELOCITY**: Target velocity in rotations per second
     *
     * ## Usage Examples:
     *
     * ### PWM Control (Duty Cycle: -1.0 to 1.0)
     * ```kotlin
     * val motor = Mongu(TalonFX(1), control = ControlType.PWM)
     * motor.move(0.5)     // 50% forward
     * motor.move(-0.3)    // 30% reverse
     * motor.move(0.0)     // stop
     * ```
     *
     * ### Voltage Control (in Volts)
     * ```kotlin
     * val motor = Mongu(TalonFX(1), control = ControlType.VOLTAGE)
     * motor.move(12.0)  // 12V forward
     * motor.move(-6.0)  // 6V reverse
     * motor.move(0.0)   // stop
     * ```
     *
     * ### Position Control (in rotations or encoder units)
     * ```kotlin
     * val motor = Mongu(TalonFX(1), control = ControlType.POSITION)
     * motor.move(10.0)   // move to position 10
     * motor.move(-5.5)   // move to position -5.5
     * ```
     *
     * ### Velocity Control (in rotations per second)
     * ```kotlin
     * val motor = Mongu(TalonFX(1), control = ControlType.VELOCITY)
     * motor.move(150.0)   // move at 150 RPS
     * motor.move(-75.0)   // move at -75 RPS
     * ```
     *
     * ## Motor Support Matrix:
     * - **TalonFX**: Supports PWM, Voltage, Position, and Velocity control
     * - **PWMTalonSRX**: Supports PWM control only
     * - **PWMSparkMax**: Supports PWM control only
     *
     * @param value The control value (meaning depends on locked control type).
     *
     * @throws UnsupportedOperationException If the control type is not supported for the current motor type.
     */
    fun move(value: Double) {
        val controlFunction: ((T, Double) -> Unit)? =
            when (control) {
                PWM -> configuration.pwmControl
                VOLTAGE -> configuration.voltageControl
                POSITION -> configuration.positionControl
                VELOCITY -> configuration.velocityControl
            }

        controlFunction?.invoke(motor, value)
            ?: throw UnsupportedOperationException(
                "Control type $control is not supported for motor type ${motor::class.simpleName}",
            )
    }

    /**
     * Stops the motor by invoking the configured stop function.
     *
     * This method should be called to immediately halt motor movement,
     * typically by setting output to zero or disabling the motor output,
     * depending on the underlying motor type and configuration.
     */
    fun stopMotor() = configuration.stop(motor)

    /**
     * Operator overload for logical NOT (`!`) to stop the motor.
     *
     * Allows using `!mongu` as shorthand for `mongu.stopMotor()`.
     */
    operator fun not() = stopMotor()
}
