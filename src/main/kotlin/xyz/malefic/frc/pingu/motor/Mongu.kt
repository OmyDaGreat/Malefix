package xyz.malefic.frc.pingu.motor

import com.ctre.phoenix6.hardware.TalonFX
import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX

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
) {
    /**
     * Holds the last configuration applied to this motor.
     * This property is updated whenever the `configure` method is called.
     * It allows retrieval of the configuration settings for inspection or reuse.
     */
    lateinit var configuration: MotorConfig<T>

    init {
        require(motor is TalonFX || motor is PWMTalonSRX) { "Unsupported motor type" }
        configure()
    }

    /**
     * Configures the motor using a DSL-style configuration block.
     *
     * @param block A lambda that applies configuration settings to the motor.
     * @throws IllegalArgumentException If the motor type is unsupported.
     */
    @Suppress("UNCHECKED_CAST")
    fun configure(block: MotorConfig<out T>.() -> Unit = {}) {
        val config =
            when (motor) {
                is TalonFX -> TalonFXConfig().apply(block as MotorConfig<TalonFX>.() -> Unit)
                is PWMTalonSRX -> PWMTalonSRXConfig().apply(block as MotorConfig<PWMTalonSRX>.() -> Unit)
                else -> throw IllegalArgumentException("Unsupported motor type")
            } as MotorConfig<T>
        config.applyTo(motor)
        configuration = config
    }
}
