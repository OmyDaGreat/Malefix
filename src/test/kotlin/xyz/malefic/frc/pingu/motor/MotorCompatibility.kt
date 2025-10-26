package xyz.malefic.frc.pingu.motor

import com.ctre.phoenix6.hardware.TalonFX
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax
import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX
import kotlin.reflect.KClass

/**
 * Interface defining motor compatibility for different control types.
 */
interface MotorCompatibility {
    /**
     * Checks if the motor supports a specific control type.
     *
     * @param controlType The control type to check.
     * @return `true` if the control type is supported, `false` otherwise.
     */
    fun supports(controlType: ControlType): Boolean

    /**
     * Retrieves a list of all control types supported by the motor.
     *
     * @return A list of supported control types.
     */
    fun supportedTypes(): List<ControlType>

    companion object {
        /**
         * Factory method to retrieve the appropriate `MotorCompatibility` implementation
         * for a given motor class.
         *
         * @param motorClass The class of the motor.
         * @return The `MotorCompatibility` implementation for the motor class.
         * @throws IllegalArgumentException If the motor class is unsupported.
         */
        fun <T : Any> forMotor(motorClass: KClass<out T>): MotorCompatibility =
            when (motorClass) {
                TalonFX::class -> TalonFXCompatibility
                PWMTalonSRX::class -> PWMTalonSRXCompatibility
                PWMSparkMax::class -> PWMSparkMaxCompatibility
                else -> throw IllegalArgumentException("Unsupported motor type: ${motorClass.simpleName}")
            }
    }
}

/**
 * Compatibility implementation for the `TalonFX` motor.
 */
object TalonFXCompatibility : MotorCompatibility {
    // Set of control types supported by the TalonFX motor.
    private val supportedTypes =
        setOf(
            ControlType.PWM,
            ControlType.VOLTAGE,
            ControlType.POSITION,
            ControlType.VELOCITY,
        )

    /**
     * Checks if the `TalonFX` motor supports a specific control type.
     *
     * @param controlType The control type to check.
     * @return `true` if the control type is supported, `false` otherwise.
     */
    override fun supports(controlType: ControlType): Boolean = controlType in supportedTypes

    /**
     * Retrieves a list of all control types supported by the `TalonFX` motor.
     *
     * @return A list of supported control types.
     */
    override fun supportedTypes(): List<ControlType> = supportedTypes.toList()
}

/**
 * Compatibility implementation for the `PWMTalonSRX` motor.
 */
object PWMTalonSRXCompatibility : MotorCompatibility {
    // Set of control types supported by the PWMTalonSRX motor.
    private val supportedTypes = setOf(ControlType.PWM)

    /**
     * Checks if the `PWMTalonSRX` motor supports a specific control type.
     *
     * @param controlType The control type to check.
     * @return `true` if the control type is supported, `false` otherwise.
     */
    override fun supports(controlType: ControlType): Boolean = controlType in supportedTypes

    /**
     * Retrieves a list of all control types supported by the `PWMTalonSRX` motor.
     *
     * @return A list of supported control types.
     */
    override fun supportedTypes(): List<ControlType> = supportedTypes.toList()
}

/**
 * Compatibility implementation for the `PWMSparkMax` motor.
 */
object PWMSparkMaxCompatibility : MotorCompatibility {
    // Set of control types supported by the PWMSparkMax motor.
    private val supportedTypes = setOf(ControlType.PWM)

    /**
     * Checks if the `PWMSparkMax` motor supports a specific control type.
     *
     * @param controlType The control type to check.
     * @return `true` if the control type is supported, `false` otherwise.
     */
    override fun supports(controlType: ControlType): Boolean = controlType in supportedTypes

    /**
     * Retrieves a list of all control types supported by the `PWMSparkMax` motor.
     *
     * @return A list of supported control types.
     */
    override fun supportedTypes(): List<ControlType> = supportedTypes.toList()
}
