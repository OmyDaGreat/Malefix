package xyz.malefic.frc.pingu.motor

import com.ctre.phoenix6.hardware.TalonFX
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax
import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX
import xyz.malefic.frc.pingu.motor.control.ControlType
import kotlin.reflect.KClass

interface MotorCompatibility {
    fun supports(controlType: ControlType): Boolean

    fun supportedTypes(): List<ControlType>

    companion object {
        fun <T : Any> forMotor(motorClass: KClass<out T>): MotorCompatibility =
            when (motorClass) {
                TalonFX::class -> TalonFXCompatibility
                PWMTalonSRX::class -> PWMTalonSRXCompatibility
                PWMSparkMax::class -> PWMSparkMaxCompatibility
                else -> throw IllegalArgumentException("Unsupported motor type: ${motorClass.simpleName}")
            }
    }
}

object TalonFXCompatibility : MotorCompatibility {
    private val supportedTypes =
        setOf(
            ControlType.PWM,
            ControlType.VOLTAGE,
            ControlType.POSITION,
            ControlType.VELOCITY,
        )

    override fun supports(controlType: ControlType): Boolean = controlType in supportedTypes

    override fun supportedTypes(): List<ControlType> = supportedTypes.toList()
}

object PWMTalonSRXCompatibility : MotorCompatibility {
    private val supportedTypes = setOf(ControlType.PWM)

    override fun supports(controlType: ControlType): Boolean = controlType in supportedTypes

    override fun supportedTypes(): List<ControlType> = supportedTypes.toList()
}

object PWMSparkMaxCompatibility : MotorCompatibility {
    private val supportedTypes = setOf(ControlType.PWM)

    override fun supports(controlType: ControlType): Boolean = controlType in supportedTypes

    override fun supportedTypes(): List<ControlType> = supportedTypes.toList()
}
