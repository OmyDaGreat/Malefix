package xyz.malefic.frc.pingu.motor

/**
 * Interface for motor configuration that includes control mappings.
 *
 * This interface defines configuration application for motors or devices of type [T].
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
}
