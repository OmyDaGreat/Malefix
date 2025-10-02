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
     * Applies the configuration to the given [PWMTalonSRX] motor.
     */
    override fun applyTo(motor: PWMTalonSRX) {
        motor.inverted = inverted
    }

    /**
     * Lambda for PWM control ([PWMTalonSRX], [Double]) -> Unit.
     */
    override val pwmControl: ((PWMTalonSRX, Double) -> Unit) = { motor, value -> motor.set(value) }
    override val voltageControl: ((PWMTalonSRX, Double) -> Unit)? = null
    override val positionControl: ((PWMTalonSRX, Double) -> Unit)? = null
}
