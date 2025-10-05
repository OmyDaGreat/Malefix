package xyz.malefic.frc.pingu.motor.cansparkmax

import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax
import xyz.malefic.frc.pingu.motor.MotorControls

/**
 * Control implementations for [PWMSparkMax] motors.
 *
 * Provides control lambdas for PWM and voltage control modes.
 */
class PWMSparkMaxControls : MotorControls<PWMSparkMax> {
    /**
     * Lambda for PWM control.
     * Sets the output of the [PWMSparkMax] motor to the specified value.
     */
    override val pwmControl: (PWMSparkMax, Double) -> Unit = { motor, value -> motor.set(value) }

    /**
     * Lambda for voltage control.
     * Sets the voltage output of the [PWMSparkMax] motor to the specified value.
     */
    override val voltageControl: (PWMSparkMax, Double) -> Unit = { motor, voltage -> motor.voltage = voltage }

    /**
     * Lambda for position control.
     * Not supported for [PWMSparkMax], so this is null.
     */
    override val positionControl: ((PWMSparkMax, Double) -> Unit)? = null

    /**
     * Lambda for velocity control.
     * Not supported for [PWMSparkMax], so this is null.
     */
    override val velocityControl: ((PWMSparkMax, Double) -> Unit)? = null

    /**
     * Lambda to stop the [PWMSparkMax] motor.
     */
    override val stop: (PWMSparkMax) -> Unit = { motor -> motor.stopMotor() }
}
