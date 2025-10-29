package xyz.malefic.frc.pingu.motor.cansparkmax

import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax
import xyz.malefic.frc.pingu.motor.MonguConfig

class PWMSparkMaxConfig : MonguConfig<PWMSparkMax> {
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
     * Applies the configuration to the given [PWMSparkMax] motor.
     */
    override fun applyTo(motor: PWMSparkMax) {
        motor.inverted = inverted
        motor.enableDeadbandElimination(deadbandElimination)
    }
}
