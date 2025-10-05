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
     * Lambda for PWM control.
     * Sets the output of the [PWMTalonSRX] motor to the specified value.
     */
    override val pwmControl: ((PWMTalonSRX, Double) -> Unit) = { motor, value -> motor.set(value) }

    /**
     * Lambda for voltage control.
     * Not supported for [PWMTalonSRX], so this is null.
     */
    override val voltageControl: (PWMTalonSRX, Double) -> Unit = { motor, voltage -> motor.voltage = voltage }

    /**
     * Lambda for position control.
     * Not supported for [PWMTalonSRX], so this is null.
     */
    override val positionControl: ((PWMTalonSRX, Double) -> Unit)? = null

    /**
     * Lambda for velocity control.
     * Not supported for [PWMTalonSRX], so this is null.
     */
    override val velocityControl: ((PWMTalonSRX, Double) -> Unit)? = null

    /**
     * Lambda to stop the [PWMTalonSRX] motor.
     */
    override val stop: (PWMTalonSRX) -> Unit = { motor -> motor.stopMotor() }
}
