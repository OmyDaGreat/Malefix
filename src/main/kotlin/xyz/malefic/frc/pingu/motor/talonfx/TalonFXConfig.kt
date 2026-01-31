package xyz.malefic.frc.pingu.motor.talonfx

import com.ctre.phoenix6.configs.TalonFXConfiguration
import com.ctre.phoenix6.hardware.TalonFX
import com.ctre.phoenix6.signals.InvertedValue
import com.ctre.phoenix6.signals.NeutralModeValue
import edu.wpi.first.units.Units
import edu.wpi.first.units.measure.Current
import xyz.malefic.frc.pingu.alert.AlertPingu
import xyz.malefic.frc.pingu.control.MagicPingu
import xyz.malefic.frc.pingu.control.Pingu
import xyz.malefic.frc.pingu.motor.MonguConfig

/**
 * Configuration class for [TalonFX] motors.
 *
 * Provides properties for configuring [TalonFX] including inversion, neutral mode, PID, current limits, and soft limits.
 */
class TalonFXConfig : MonguConfig<TalonFX> {
    /**
     * Optional custom [TalonFXConfiguration] to use as a base.
     */
    var talonConfig: TalonFXConfiguration? = null

    /**
     * Motor inversion setting ([InvertedValue]).
     */
    var inverted: InvertedValue = InvertedValue.Clockwise_Positive

    /**
     * Neutral mode ([NeutralModeValue]).
     */
    var neutralMode: NeutralModeValue = NeutralModeValue.Coast

    /**
     * Deadband for duty cycle output.
     */
    var dutyCycleNeutralDeadband: Double = 0.04

    /**
     * Optional PID configuration ([Pingu]).
     */
    var pingu = Pingu()

    /**
     * Pair of supply and stator current limits as measured values (nullable).
     * First value is supply current limit, second is stator current limit.
     */
    var currentLimits: Pair<Current?, Current?>? = null

    /**
     * Pair of forward and reverse soft limits in rotations (nullable).
     * First value is forward soft limit, second is reverse soft limit.
     */
    var softLimits: Pair<Double?, Double?>? = null

    /**
     * Pair of open-loop and closed-loop ramp rates in seconds.
     * First value is open-loop ramp, second is closed-loop ramp.
     */
    var loopRamp: Pair<Double, Double> = 0.0 to 0.0

    /**
     * Optional Motion Magic configuration.
     */
    var motionMagicPingu = MagicPingu()

    /**
     * Optional name for the motor (for alarm/logging/debugging).
     */
    var name: String? = null

    /**
     * Whether to set an alarm for this motor.
     * Returns true only if alarms are enabled and a name is set.
     */
    var setAlarm: Boolean = true
        get() = field && name != null

    /**
     * Optional lambda for additional [TalonFXConfiguration] customization.
     */
    var extraConfig: (TalonFXConfiguration.() -> Unit)? = null

    /**
     * Tracks whether the configuration is being applied for the first time.
     * Used to ensure certain initialization steps (like setting initial position or alarms)
     * are only performed once.
     */
    private var isFirstApply = true

    /**
     * Applies the configuration to the given [TalonFX] motor.
     */
    override fun applyTo(motor: TalonFX) {
        val config = talonConfig ?: TalonFXConfiguration()

        config.MotorOutput.apply {
            Inverted = inverted
            NeutralMode = neutralMode
            DutyCycleNeutralDeadband = dutyCycleNeutralDeadband
        }

        pingu.let {
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
                SupplyCurrentLimit = supply?.`in`(Units.Amps) ?: 0.0
                SupplyCurrentLimitEnable = supply != null
                StatorCurrentLimit = stator?.`in`(Units.Amps) ?: 0.0
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
        } ?: run {
            config.SoftwareLimitSwitch.apply {
                ForwardSoftLimitEnable = false
                ReverseSoftLimitEnable = false
            }
        }

        config.OpenLoopRamps.DutyCycleOpenLoopRampPeriod = loopRamp.first
        config.ClosedLoopRamps.DutyCycleClosedLoopRampPeriod = loopRamp.second

        motionMagicPingu.let {
            config.MotionMagic.apply {
                MotionMagicCruiseVelocity = it.velocity
                MotionMagicAcceleration = it.acceleration
                MotionMagicJerk = it.jerk
            }
        }

        extraConfig?.invoke(config)
        motor.configurator.apply(config)

        if (isFirstApply) {
            motor.setPosition(0.0)

            if (setAlarm) {
                AlertPingu.add(
                    motor,
                    name!!,
                )
            }
            isFirstApply = false
        }
    }
}
