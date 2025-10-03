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
     * Lambda to stop the [PWMSparkMax] motor.
     */
    override val stop: (PWMSparkMax) -> Unit = { motor -> motor.stopMotor() }
}
