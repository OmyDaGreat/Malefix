package xyz.malefic.frc.pingu.motor

/**
 * Interface for motor configuration that includes control mappings.
 *
 * This interface defines configuration application and provides access to motor controls.
 * Control logic is delegated to a [MotorControls] instance for better separation of concerns.
 *
 * @param T The type of the motor or device being controlled.
 * @property controls The [MotorControls] instance containing all control lambdas for this motor type.
 */
interface MonguConfig<T : Any> {
    /**
     * Applies the configuration to the given motor.
     *
     * @param motor The motor instance to configure.
     */
    fun applyTo(motor: T)

    /**
     * The motor controls instance containing all control lambdas.
     */
    val controls: MotorControls<T>

    /**
     * Lambda function for controlling the motor using PWM (Pulse Width Modulation).
     *
     * Delegates to [controls.pwmControl].
     *
     * @see MotorControls.pwmControl
     */
    val pwmControl: ((T, Double) -> Unit)?
        get() = controls.pwmControl

    /**
     * Lambda function for controlling the motor using voltage.
     *
     * Delegates to [controls.voltageControl].
     *
     * @see MotorControls.voltageControl
     */
    val voltageControl: ((T, Double) -> Unit)?
        get() = controls.voltageControl

    /**
     * Lambda function for controlling the motor using a position value.
     *
     * Delegates to [controls.positionControl].
     *
     * @see MotorControls.positionControl
     */
    val positionControl: ((T, Double) -> Unit)?
        get() = controls.positionControl

    /**
     * Lambda function for controlling the motor using a velocity value.
     *
     * Delegates to [controls.velocityControl].
     *
     * @see MotorControls.velocityControl
     */
    val velocityControl: ((T, Double) -> Unit)?
        get() = controls.velocityControl

    /**
     * Lambda function to stop the motor.
     *
     * Delegates to [controls.stop].
     *
     * @see MotorControls.stop
     */
    val stop: (T) -> Unit
        get() = controls.stop
}
