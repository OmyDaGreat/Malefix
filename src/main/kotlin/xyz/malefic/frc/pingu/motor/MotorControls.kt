package xyz.malefic.frc.pingu.motor

/**
 * Interface defining control functions for a motor type.
 *
 * This interface encapsulates all control lambdas for a specific motor type,
 * separating control logic from configuration logic for better readability.
 *
 * @param T The type of motor being controlled.
 */
interface MotorControls<T : Any> {
    /**
     * Lambda function for controlling the motor using PWM (Pulse Width Modulation).
     *
     * This function takes two parameters:
     * - An instance of type [T] representing the motor or device.
     * - A [Double] value representing the PWM signal.
     *
     * If `null`, PWM control is not supported.
     */
    val pwmControl: ((T, Double) -> Unit)?

    /**
     * Lambda function for controlling the motor using voltage.
     *
     * This function takes two parameters:
     * - An instance of type [T] representing the motor or device.
     * - A [Double] value representing the voltage to be applied.
     *
     * If `null`, voltage control is not supported.
     */
    val voltageControl: ((T, Double) -> Unit)?

    /**
     * Lambda function for controlling the motor using a position value.
     *
     * This function takes two parameters:
     * - An instance of type [T] representing the motor or device.
     * - A [Double] value representing the target position.
     *
     * If `null`, position control is not supported.
     */
    val positionControl: ((T, Double) -> Unit)?

    /**
     * Lambda function for controlling the motor using a velocity value.
     *
     * This function takes two parameters:
     * - An instance of type [T] representing the motor or device.
     * - A [Double] value representing the target velocity.
     *
     * If `null`, velocity control is not supported.
     */
    val velocityControl: ((T, Double) -> Unit)?

    /**
     * Lambda function to stop the motor.
     *
     * This function should perform any necessary actions to safely stop the motor.
     */
    val stop: (T) -> Unit
}
