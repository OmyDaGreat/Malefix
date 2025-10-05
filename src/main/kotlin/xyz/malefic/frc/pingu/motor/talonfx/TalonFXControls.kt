package xyz.malefic.frc.pingu.motor.talonfx

import com.ctre.phoenix6.controls.PositionVoltage
import com.ctre.phoenix6.controls.VelocityVoltage
import com.ctre.phoenix6.hardware.TalonFX
import xyz.malefic.frc.pingu.motor.MotorControls

/**
 * Control implementations for [TalonFX] motors.
 *
 * Provides control lambdas for PWM, voltage, position, and velocity control modes.
 *
 * @property positionVoltage Pre-allocated [PositionVoltage] control object for position control mode.
 * @property velocityVoltage Pre-allocated [VelocityVoltage] control object for velocity control mode.
 */
class TalonFXControls(
    val positionVoltage: PositionVoltage = PositionVoltage(0.0),
    val velocityVoltage: VelocityVoltage = VelocityVoltage(0.0),
) : MotorControls<TalonFX> {
    /**
     * Lambda for PWM control.
     * Sets the output of the [TalonFX] motor to the specified value.
     */
    override val pwmControl: (TalonFX, Double) -> Unit = { motor, value -> motor.set(value) }

    /**
     * Lambda for voltage control.
     * Sets the voltage of the [TalonFX] motor to the specified value.
     */
    override val voltageControl: (TalonFX, Double) -> Unit = { motor, value -> motor.setVoltage(value) }

    /**
     * Lambda for position control.
     * Sets the position of the [TalonFX] motor to the specified value.
     */
    override val positionControl: (TalonFX, Double) -> Unit = { motor, value -> motor.setControl(positionVoltage.withPosition(value)) }

    /**
     * Lambda for velocity control.
     * Sets the velocity of the [TalonFX] motor to the specified value.
     */
    override val velocityControl: (TalonFX, Double) -> Unit = { motor, value -> motor.setControl(velocityVoltage.withVelocity(value)) }

    /**
     * Lambda to stop the [TalonFX] motor.
     */
    override val stop: (TalonFX) -> Unit = { motor -> motor.stopMotor() }
}
