package xyz.malefic.frc.pingu.motor.pwmtalonsrx

import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX
import xyz.malefic.frc.pingu.motor.MonguConfig

/**
 * Configuration class for [PWMTalonSRX] motors.
 */
class PWMTalonSRXConfig : MonguConfig<PWMTalonSRX> {
    /**
     * Indicates whether the motor direction is inverted.
     */
    var inverted = false

    /**
     * Enables or disables deadband elimination for the motor.
     * When true, small input values are ignored to reduce motor jitter.
     */
    var deadbandElimination = false

    /**
     * Applies the configuration to the given [PWMTalonSRX] motor.
     */
    override fun applyTo(motor: PWMTalonSRX) {
        motor.inverted = inverted
        motor.enableDeadbandElimination(deadbandElimination)
    }

    /**
     * The motor controls instance containing all control lambdas for [PWMTalonSRX].
     */
    override val controls: PWMTalonSRXControls = PWMTalonSRXControls()
}
