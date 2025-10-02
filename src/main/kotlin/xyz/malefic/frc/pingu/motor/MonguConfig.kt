package xyz.malefic.frc.pingu.motor

/**
 * Interface for motor configuration that includes control mappings.
 *
 * This interface defines both configuration application and control mappings for a motor type.
 * Each control mechanism is represented as a nullable lambda function that takes an instance
 * of type `T` and a `Double` value as parameters.
 *
 * @param T The type of the motor or device being controlled.
 */
interface MonguConfig<T : Any> {
    /**
     * Applies the configuration to the given motor.
     *
     * @param motor The motor instance to configure.
     */
    fun applyTo(motor: T)

    /**
     * Lambda function for controlling the motor using PWM (Pulse Width Modulation).
     *
     * This function takes two parameters:
     * - An instance of type `T` representing the motor or device.
     * - A `Double` value representing the PWM signal.
     *
     * If `null`, PWM control is not supported.
     */
    val pwmControl: ((T, Double) -> Unit)?

    /**
     * Lambda function for controlling the motor using voltage.
     *
     * This function takes two parameters:
     * - An instance of type `T` representing the motor or device.
     * - A `Double` value representing the voltage to be applied.
     *
     * If `null`, voltage control is not supported.
     */
    val voltageControl: ((T, Double) -> Unit)?

    /**
     * Lambda function for controlling the motor's position.
     *
     * This function takes two parameters:
     * - An instance of type `T` representing the motor or device.
     * - A `Double` value representing the target position.
     *
     * If `null`, position control is not supported.
     */
    val positionControl: ((T, Double) -> Unit)?
}
