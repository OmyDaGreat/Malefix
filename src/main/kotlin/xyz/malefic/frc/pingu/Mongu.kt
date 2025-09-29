package xyz.malefic.frc.pingu

import com.ctre.phoenix6.configs.TalonFXConfiguration
import com.ctre.phoenix6.hardware.TalonFX
import com.ctre.phoenix6.signals.InvertedValue
import com.ctre.phoenix6.signals.NeutralModeValue
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

/**
 * A functional interface for motor configuration.
 *
 * @param T The type of motor being configured.
 */
fun interface MotorConfig<T : Any> {
    /**
     * Applies the configuration to the given motor.
     *
     * @param motor The motor to configure.
     */
    fun applyTo(motor: T)
}

/**
 * Configuration class for [TalonFX] motors.
 */
class TalonFXConfig : MotorConfig<TalonFX> {
    /**
     * Optional [TalonFX] configuration object.
     */
    var talonConfig: TalonFXConfiguration? = null

    /**
     * Motor inversion setting.
     */
    var inverted: InvertedValue = InvertedValue.Clockwise_Positive

    /**
     * Neutral mode setting.
     */
    var neutralMode: NeutralModeValue = NeutralModeValue.Coast

    /**
     * Deadband for duty cycle.
     */
    var dutyCycleNeutralDeadband: Double = 0.04

    /**
     * PID configuration object.
     */
    var pingu: Pingu? = null

    /**
     * Supply and stator current limits.
     */
    var currentLimits: Pair<Double?, Double?>? = null

    /**
     * Forward and reverse soft limits.
     */
    var softLimits: Pair<Double?, Double?>? = null

    /**
     * Open and closed loop ramp rates.
     */
    var loopRamp: Pair<Double, Double> = 0.0 to 0.0

    /**
     * Motion magic configuration.
     */
    var motionMagicPingu: MagicPingu? = null

    /**
     * Additional custom configuration.
     */
    var extraConfig: (TalonFXConfiguration.() -> Unit)? = null

    /**
     * Applies the configuration to the given [TalonFX] motor.
     *
     * @param motor The [TalonFX] motor to configure.
     */
    override fun applyTo(motor: TalonFX) {
        val config = talonConfig ?: TalonFXConfiguration()

        config.MotorOutput.apply {
            Inverted = inverted
            NeutralMode = neutralMode
            DutyCycleNeutralDeadband = dutyCycleNeutralDeadband
        }

        pingu?.let {
            config.Slot0.apply {
                kP = it.p
                kI = it.i
                kD = it.d
                kV = it.v
                kS = it.s
                kG = it.g
            }
        }

        config.CurrentLimits.apply {
            currentLimits?.let { (supply, stator) ->
                SupplyCurrentLimit = supply ?: 0.0
                SupplyCurrentLimitEnable = supply != null
                StatorCurrentLimit = stator ?: 0.0
                StatorCurrentLimitEnable = stator != null
            } ?: run {
                SupplyCurrentLimitEnable = false
                StatorCurrentLimitEnable = false
            }
        }

        softLimits?.let { (forward, reverse) ->
            config.SoftwareLimitSwitch.apply {
                ForwardSoftLimitThreshold = forward ?: 0.0
                ForwardSoftLimitEnable = forward != null
                ReverseSoftLimitThreshold = reverse ?: 0.0
                ReverseSoftLimitEnable = reverse != null
            }
        }

        config.OpenLoopRamps.DutyCycleOpenLoopRampPeriod = loopRamp.first
        config.ClosedLoopRamps.DutyCycleClosedLoopRampPeriod = loopRamp.second

        motionMagicPingu?.let {
            config.MotionMagic.apply {
                MotionMagicCruiseVelocity = it.velocity
                MotionMagicAcceleration = it.acceleration
                MotionMagicJerk = it.jerk
            }
        }

        extraConfig?.invoke(config)
        motor.configurator.apply(config)
    }
}

/**
 * Configuration class for [PWMTalonSRX] motors.
 */
class PWMTalonSRXConfig : MotorConfig<PWMTalonSRX> {
    /**
     * Motor inversion setting.
     * If true, the motor direction is inverted.
     */
    var inverted = false

    /**
     * Applies the configuration to the given [PWMTalonSRX] motor.
     *
     * @param motor The PWMTalonSRX motor to configure.
     */
    override fun applyTo(motor: PWMTalonSRX) {
        motor.inverted = inverted
    }
}
