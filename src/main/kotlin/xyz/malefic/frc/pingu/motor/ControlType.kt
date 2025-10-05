package xyz.malefic.frc.pingu.motor

/**
 * Enum representing the different types of motor control modes.
 */
enum class ControlType {
    /**
     * Pulse Width Modulation (PWM) control type.
     */
    PWM,

    /**
     * Voltage-based control type.
     */
    VOLTAGE,

    /**
     * Position-based control type.
     */
    POSITION,

    /**
     * Velocity-based control type.
     */
    VELOCITY,
}
