package xyz.malefic.frc.swerve

import com.ctre.phoenix6.configs.TalonFXConfiguration
import com.ctre.phoenix6.controls.PositionVoltage
import com.ctre.phoenix6.controls.VelocityVoltage
import com.ctre.phoenix6.signals.InvertedValue
import com.ctre.phoenix6.signals.NeutralModeValue
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.kinematics.SwerveModulePosition
import edu.wpi.first.math.kinematics.SwerveModuleState
import xyz.malefic.frc.pingu.control.Pingu
import xyz.malefic.frc.pingu.encoder.Engu
import xyz.malefic.frc.pingu.motor.talonfx.TonguFX
import kotlin.math.PI

/**
 * Represents a single swerve module with drive and steer motors.
 *
 * This class manages a swerve module consisting of:
 * - A drive motor (for wheel rotation) using [TonguFX]
 * - A steer motor (for changing module direction) using [TonguFX]
 * - An [Engu] CANcoder for absolute angle measurement
 * - [Pingu] PID controllers for drive and steer control
 *
 * ## Usage Example:
 * ```kotlin
 * val module = SwerveModule(
 *     driveMotorId = 1,
 *     steerMotorId = 2,
 *     canCoderId = 3,
 *     angleOffset = Rotation2d.fromDegrees(45.0)
 * )
 *
 * module.configure {
 *     driveGearRatio = 6.75
 *     steerGearRatio = 150.0 / 7.0
 *     wheelDiameterMeters = 0.1016
 *     drivePingu.apply {
 *         p = 0.1
 *         v = 0.12
 *         s = 0.2
 *     }
 *     steerPingu.apply {
 *         p = 100.0
 *         d = 0.1
 *     }
 * }
 *
 * // Set desired state
 * val desiredState = SwerveModuleState(2.0, Rotation2d.fromDegrees(90.0))
 * module.setDesiredState(desiredState)
 * ```
 *
 * @property driveMotorId CAN ID for the drive motor.
 * @property steerMotorId CAN ID for the steer motor.
 * @property canCoderId CAN ID for the CANcoder.
 * @property angleOffset Absolute encoder offset for the module's zero position.
 */
class SwerveModule(
    driveMotorId: Int,
    steerMotorId: Int,
    canCoderId: Int,
    val angleOffset: Rotation2d = Rotation2d(),
) {
    /**
     * Configuration for a swerve module.
     *
     * @property driveGearRatio Gear ratio for the drive motor (motor rotations per wheel rotation).
     * @property steerGearRatio Gear ratio for the steer motor (motor rotations per module rotation).
     * @property wheelDiameterMeters Diameter of the wheel in meters.
     * @property drivePingu [Pingu] PID controller for drive motor velocity control.
     * @property steerPingu [Pingu] PID controller for steer motor position control.
     * @property driveCurrentLimit Current limit for drive motor in amps.
     * @property steerCurrentLimit Current limit for steer motor in amps.
     * @property driveInverted Whether the drive motor is inverted.
     * @property steerInverted Whether the steer motor is inverted.
     */
    data class Config(
        var driveGearRatio: Double = 6.75,
        var steerGearRatio: Double = 150.0 / 7.0,
        var wheelDiameterMeters: Double = 0.1016,
        val drivePingu: Pingu = Pingu(p = 0.1),
        val steerPingu: Pingu = Pingu(p = 100.0),
        var driveCurrentLimit: Double = 40.0,
        var steerCurrentLimit: Double = 30.0,
        var driveInverted: Boolean = false,
        var steerInverted: Boolean = true,
    )

    private var config = Config()

    private val driveMotor =
        TonguFX(
            driveMotorId,
            VelocityVoltage(0.0),
            { withVelocity(it) },
        )

    private val steerMotor =
        TonguFX(
            steerMotorId,
            PositionVoltage(0.0),
            { withPosition(it) },
        )

    private val canCoder = Engu(canCoderId)

    private val wheelCircumference: Double
        get() = config.wheelDiameterMeters * PI

    /**
     * Configures the swerve module using a DSL-style configuration block.
     *
     * ```kotlin
     * module.configure {
     *     driveGearRatio = 6.75
     *     steerGearRatio = 21.43
     *     wheelDiameterMeters = 0.1016
     *     drivePingu.apply {
     *         p = 0.1
     *         v = 0.12
     *         s = 0.2
     *     }
     *     steerPingu.apply {
     *         p = 100.0
     *         d = 0.1
     *     }
     * }
     * ```
     *
     * @param block Configuration block to apply settings.
     */
    fun configure(block: Config.() -> Unit) {
        config.apply(block)
        applyConfiguration()
    }

    private fun applyConfiguration() {
        // Configure CANcoder using Engu's simplified method
        canCoder.configureSwerve(angleOffset, clockwisePositive = false)

        // Configure drive motor with Pingu
        val driveConfig =
            TalonFXConfiguration().apply {
                MotorOutput.apply {
                    Inverted =
                        if (config.driveInverted) {
                            InvertedValue.Clockwise_Positive
                        } else {
                            InvertedValue.CounterClockwise_Positive
                        }
                    NeutralMode = NeutralModeValue.Brake
                }
                CurrentLimits.apply {
                    SupplyCurrentLimit = config.driveCurrentLimit
                    SupplyCurrentLimitEnable = true
                }
                Slot0.apply {
                    kP = config.drivePingu.p
                    kI = config.drivePingu.i
                    kD = config.drivePingu.d
                    kV = config.drivePingu.v
                    kS = config.drivePingu.s
                }
            }
        driveMotor.configurator.apply(driveConfig)

        // Configure steer motor with Pingu
        val steerConfig =
            TalonFXConfiguration().apply {
                MotorOutput.apply {
                    Inverted =
                        if (config.steerInverted) {
                            InvertedValue.Clockwise_Positive
                        } else {
                            InvertedValue.CounterClockwise_Positive
                        }
                    NeutralMode = NeutralModeValue.Brake
                }
                CurrentLimits.apply {
                    SupplyCurrentLimit = config.steerCurrentLimit
                    SupplyCurrentLimitEnable = true
                }
                Slot0.apply {
                    kP = config.steerPingu.p
                    kI = config.steerPingu.i
                    kD = config.steerPingu.d
                }
                ClosedLoopGeneral.apply {
                    ContinuousWrap = true
                }
            }
        steerMotor.configurator.apply(steerConfig)

        // Sync steer motor position with CANcoder
        resetToAbsolute()
    }

    /**
     * Resets the steer motor position to match the absolute encoder.
     */
    fun resetToAbsolute() {
        val absolutePosition = canCoder.rotations
        steerMotor.setPosition(absolutePosition * config.steerGearRatio)
    }

    /**
     * Gets the current angle of the module from the CANcoder.
     *
     * Uses [Engu.rotation] for convenient [Rotation2d] access.
     *
     * @return Current module angle as [Rotation2d].
     */
    fun getAngle(): Rotation2d = canCoder.rotation

    /**
     * Gets the current position of the swerve module.
     *
     * @return Current [SwerveModulePosition] with distance and angle.
     */
    fun getPosition(): SwerveModulePosition {
        val drivePositionRotations = driveMotor.position.valueAsDouble
        val drivePositionMeters = (drivePositionRotations / config.driveGearRatio) * wheelCircumference
        return SwerveModulePosition(drivePositionMeters, getAngle())
    }

    /**
     * Gets the current state of the swerve module.
     *
     * @return Current [SwerveModuleState] with velocity and angle.
     */
    fun getState(): SwerveModuleState {
        val driveVelocityRPS = driveMotor.velocity.valueAsDouble
        val driveVelocityMPS = (driveVelocityRPS / config.driveGearRatio) * wheelCircumference
        return SwerveModuleState(driveVelocityMPS, getAngle())
    }

    /**
     * Sets the desired state for the swerve module.
     *
     * Optimizes the target state to minimize rotation and applies the state to the motors.
     *
     * @param desiredState The desired [SwerveModuleState] to achieve.
     * @param isOpenLoop Whether to use open loop control (PWM) instead of closed loop.
     */
    fun setDesiredState(
        desiredState: SwerveModuleState,
        isOpenLoop: Boolean = false,
    ) {
        val optimizedState = desiredState.apply { optimize(getAngle()) }

        // Set steer motor position
        val steerPositionRotations = optimizedState.angle.rotations * config.steerGearRatio
        steerMotor.control(steerPositionRotations)

        // Set drive motor velocity
        val driveVelocityMPS = optimizedState.speedMetersPerSecond
        if (isOpenLoop) {
            val percentOutput = driveVelocityMPS / getMaxLinearVelocityMPS()
            driveMotor.movePWM(percentOutput)
        } else {
            val driveVelocityRPS = (driveVelocityMPS / wheelCircumference) * config.driveGearRatio
            driveMotor.control(driveVelocityRPS)
        }
    }

    /**
     * Sets the module to a specific voltage for characterization.
     *
     * @param voltage Voltage to apply to drive motor.
     * @param angle Angle to set the steer motor to.
     */
    fun setVoltage(
        voltage: Double,
        angle: Rotation2d,
    ) {
        val steerPositionRotations = angle.rotations * config.steerGearRatio
        steerMotor.control(steerPositionRotations)
        driveMotor.setVoltage(voltage)
    }

    /**
     * Stops both motors.
     */
    fun stop() {
        driveMotor.stopMotor()
        steerMotor.stopMotor()
    }

    /**
     * Gets the maximum linear velocity in meters per second.
     *
     * Assumes a theoretical max RPS of 100 for the TalonFX.
     *
     * @return Maximum linear velocity in m/s.
     */
    fun getMaxLinearVelocityMPS(): Double {
        val maxMotorRPS = 100.0
        return (maxMotorRPS / config.driveGearRatio) * wheelCircumference
    }

    /**
     * Gets the drive motor velocity in rotations per second.
     *
     * @return Drive motor velocity in RPS.
     */
    fun getDriveVelocity(): Double = driveMotor.velocity.valueAsDouble

    /**
     * Gets the drive motor position in rotations.
     *
     * @return Drive motor position in rotations.
     */
    fun getDrivePosition(): Double = driveMotor.position.valueAsDouble

    /**
     * Gets the steer motor position in rotations.
     *
     * @return Steer motor position in rotations.
     */
    fun getSteerPosition(): Double = steerMotor.position.valueAsDouble

    /**
     * Provides access to the module's [Engu] encoder for advanced operations.
     *
     * @return The [Engu] CANcoder instance.
     */
    val encoder: Engu
        get() = canCoder
}
