package xyz.malefic.frc.pingu.motor.pwmtalonsrx

import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX
import xyz.malefic.frc.pingu.motor.MotorControls

/**
 * Control implementations for [PWMTalonSRX] motors.
 *
 * Provides control lambdas for PWM and voltage control modes.
 */
class PWMTalonSRXControls : MotorControls<PWMTalonSRX> {
    /**
     * Lambda for PWM control.
     * Sets the output of the [PWMTalonSRX] motor to the specified value.
     */
    override val pwmControl: ((PWMTalonSRX, Double) -> Unit) = { motor, value -> motor.set(value) }

    /**
     * Lambda for voltage control.
     * Sets the voltage output of the [PWMTalonSRX] motor to the specified value.
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
