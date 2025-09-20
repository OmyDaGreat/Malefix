package xyz.malefic.frc.extension

import com.ctre.phoenix6.configs.TalonFXConfiguration
import com.ctre.phoenix6.hardware.TalonFX
import com.ctre.phoenix6.signals.InvertedValue
import com.ctre.phoenix6.signals.NeutralModeValue
import xyz.malefic.frc.pingu.MagicPingu
import xyz.malefic.frc.pingu.Pingu

/**
 * Extension function to configure a TalonFX motor with nearly all relevant options.
 *
 * @param inverted Motor inversion (default: Clockwise_Positive)
 * @param neutralMode Neutral mode (default: Brake)
 * @param currentLimits Pair of supply and stator current limits (if null, disables current limiting)
 * @param pingu Pingu instance for PID values
 * @param limitThresholds Pair of forward and reverse soft limit thresholds (if null, disables limit)
 * @param dutyCycleNeutralDeadband Deadband for neutral (default: 0.001)
 * @param closedLoopRamp Closed-loop ramp in seconds (default: 0.0)
 * @param openLoopRamp Open-loop ramp in seconds (default: 0.0)
 * @param motionMagicPingu Motion Magic Pingu (if null, not set)
 * @param extraConfig Optional lambda for further TalonFXConfiguration customization.
 */
fun TalonFX.configureWithDefaults(
    pingu: Pingu,
    inverted: InvertedValue = InvertedValue.Clockwise_Positive,
    neutralMode: NeutralModeValue = NeutralModeValue.Brake,
    currentLimits: Pair<Double?, Double?>? = Pair(40.0, 40.0),
    limitThresholds: Pair<Double?, Double?>? = null,
    dutyCycleNeutralDeadband: Double = 0.001,
    closedLoopRamp: Double = 0.0,
    openLoopRamp: Double = 0.0,
    motionMagicPingu: MagicPingu? = null,
    extraConfig: (TalonFXConfiguration.() -> Unit)? = null,
) {
    val config = TalonFXConfiguration()

    // Motor Output
    config.MotorOutput.Inverted = inverted
    config.MotorOutput.NeutralMode = neutralMode
    config.MotorOutput.DutyCycleNeutralDeadband = dutyCycleNeutralDeadband

    // PID via Pingu
    pingu.let {
        config.Slot0.kP = it.p
        config.Slot0.kI = it.i
        config.Slot0.kD = it.d
        config.Slot0.kV = it.v ?: 0.0
        config.Slot0.kS = it.s ?: 0.0
        config.Slot0.kG = it.g ?: 0.0
    }

    // Current Limits
    currentLimits?.let { _ ->
        currentLimits.first?.let {
            config.CurrentLimits.SupplyCurrentLimit = it
            config.CurrentLimits.SupplyCurrentLimitEnable = true
        } ?: run {
            config.CurrentLimits.SupplyCurrentLimitEnable = false
        }
        currentLimits.second?.let {
            config.CurrentLimits.StatorCurrentLimit = it
            config.CurrentLimits.StatorCurrentLimitEnable = true
        } ?: run {
            config.CurrentLimits.StatorCurrentLimitEnable = false
        }
    } ?: run {
        config.CurrentLimits.SupplyCurrentLimitEnable = false
        config.CurrentLimits.StatorCurrentLimitEnable = false
    }

    // Software Limit Switches
    limitThresholds?.let {
        limitThresholds.first?.let {
            config.SoftwareLimitSwitch.ForwardSoftLimitThreshold = it
            config.SoftwareLimitSwitch.ForwardSoftLimitEnable = true
        }
        limitThresholds.second?.let {
            config.SoftwareLimitSwitch.ReverseSoftLimitThreshold = it
            config.SoftwareLimitSwitch.ReverseSoftLimitEnable = true
        }
    }

    // Closed loop and open loop ramping
    config.ClosedLoopRamps.DutyCycleClosedLoopRampPeriod = closedLoopRamp
    config.OpenLoopRamps.DutyCycleOpenLoopRampPeriod = openLoopRamp

    // Motion Magic
    motionMagicPingu?.let {
        config.MotionMagic.MotionMagicCruiseVelocity = it.velocity
        config.MotionMagic.MotionMagicAcceleration = it.acceleration
        config.MotionMagic.MotionMagicJerk = it.jerk
    }

    // Extra customization if needed
    extraConfig?.let { config.it() }

    // Apply configuration
    this.configurator.apply(config)
}
