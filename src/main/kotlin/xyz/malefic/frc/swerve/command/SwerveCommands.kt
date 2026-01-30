package xyz.malefic.frc.swerve.command

import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.math.kinematics.SwerveModuleState
import edu.wpi.first.wpilibj2.command.Command
import xyz.malefic.frc.pingu.control.Pingu
import xyz.malefic.frc.swerve.SwerveDrive
import java.lang.Math.toRadians

/**
 * Command that locks the swerve modules in an X formation to prevent pushing.
 *
 * This command is useful for defense or when the robot needs to resist being pushed.
 * All modules are oriented at 45-degree angles forming an X pattern.
 *
 * ## Usage Example:
 * ```kotlin
 * val lockCommand = LockSwerve(drive)
 * controller.x().onTrue(lockCommand)
 * ```
 *
 * @property swerveDrive The swerve drive subsystem.
 */
class LockSwerve(
    private val swerveDrive: SwerveDrive,
) : Command() {
    init {
        addRequirements(swerveDrive)
    }

    override fun initialize() {
        swerveDrive.drive(ChassisSpeeds())
    }

    override fun execute() {
        // Lock modules in X formation
        swerveDrive.setModuleStates(
            arrayOf(
                SwerveModuleState(0.0, Rotation2d.fromDegrees(45.0)),
                SwerveModuleState(0.0, Rotation2d.fromDegrees(-45.0)),
                SwerveModuleState(0.0, Rotation2d.fromDegrees(-45.0)),
                SwerveModuleState(0.0, Rotation2d.fromDegrees(45.0)),
            ),
        )
    }

    override fun end(interrupted: Boolean) {
        swerveDrive.stop()
    }

    override fun isFinished(): Boolean = false
}

/**
 * Command to reset the gyro heading to zero or a specific angle.
 *
 * ## Usage Example:
 * ```kotlin
 * val resetGyroCommand = ResetGyro(drive)
 * controller.start().onTrue(resetGyroCommand)
 * ```
 *
 * @property swerveDrive The swerve drive subsystem.
 * @property angle The angle to reset to (defaults to 0 degrees).
 */
class ResetGyro(
    private val swerveDrive: SwerveDrive,
    private val angle: Rotation2d = Rotation2d(),
) : Command() {
    init {
        addRequirements(swerveDrive)
    }

    override fun initialize() {
        swerveDrive.setGyro(angle)
    }

    override fun isFinished(): Boolean = true
}

/**
 * Command to drive the robot at a specific heading angle.
 *
 * This command allows the robot to drive in any direction while maintaining
 * a specific orientation, useful for aiming at targets while moving.
 *
 * ## Usage Example:
 * ```kotlin
 * val driveToAngle = DriveToAngle(
 *     swerveDrive = drive,
 *     xSupplier = { controller.leftY },
 *     ySupplier = { controller.leftX },
 *     targetAngle = Rotation2d.fromDegrees(180.0)
 * ).apply {
 *     configure {
 *         kP = 0.05
 *         kD = 0.001
 *         tolerance = 2.0
 *     }
 * }
 * ```
 *
 * @property swerveDrive The swerve drive subsystem.
 * @property xSupplier Supplier for forward/backward input (-1.0 to 1.0).
 * @property ySupplier Supplier for left/right input (-1.0 to 1.0).
 * @property targetAngle The target angle to maintain.
 */
class DriveToAngle(
    private val swerveDrive: SwerveDrive,
    private val xSupplier: () -> Double,
    private val ySupplier: () -> Double,
    private val targetAngle: Rotation2d,
) : Command() {
    /**
     * Configuration for drive to angle control.
     *
     * @property pingu Pingu controller for angle correction.
     * @property tolerance Tolerance in degrees for considering the angle reached.
     * @property maxRotationSpeed Maximum rotation speed (0.0 to 1.0).
     */
    data class Config(
        var pingu: Pingu = Pingu(0.05, 0.0, 0.001),
        var tolerance: Double = 1.0,
        var maxRotationSpeed: Double = 0.5,
    )

    private var config = Config()
    private var pid =
        config.pingu.pidController.apply {
            setTolerance(config.tolerance)
        }

    init {
        addRequirements(swerveDrive)
    }

    /**
     * Configures the drive to angle command using a DSL-style configuration block.
     *
     * ```kotlin
     * command.configure {
     *     pingu = Pingu(0.05, 0.0, 0.001)
     *     tolerance = 2.0
     * }
     * ```
     *
     * @param block Configuration block to apply settings.
     */
    fun configure(block: Config.() -> Unit) {
        config.apply(block)
        pid =
            config.pingu.pidController.apply {
                setTolerance(toRadians(config.tolerance))
            }
    }

    override fun initialize() {
        pid.reset()
    }

    override fun execute() {
        val currentAngle = swerveDrive.getGyroRotation()

        val rotationSpeed =
            pid
                .calculate(currentAngle.radians, targetAngle.radians)
                .coerceIn(-config.maxRotationSpeed, config.maxRotationSpeed)

        swerveDrive.drive(
            xSupplier(),
            ySupplier(),
            rotationSpeed,
            fieldOriented = true,
        )
    }

    override fun end(interrupted: Boolean) = swerveDrive.stop()

    override fun isFinished(): Boolean = pid.atSetpoint()
}

/**
 * Command to point the robot at a specific angle while remaining stationary.
 *
 * ## Usage Example:
 * ```kotlin
 * val turnToAngle = TurnToAngle(
 *     swerveDrive = drive,
 *     targetAngle = Rotation2d.fromDegrees(90.0)
 * ).apply {
 *     configure {
 *         kP = 0.05
 *         tolerance = 1.0
 *     }
 * }
 * ```
 *
 * @property swerveDrive The swerve drive subsystem.
 * @property targetAngle The target angle to turn to.
 */
class TurnToAngle(
    private val swerveDrive: SwerveDrive,
    private val targetAngle: Rotation2d,
) : Command() {
    /**
     * Configuration for turn to angle control.
     *
     * @property pingu Pingu controller for angle correction.
     * @property tolerance Tolerance in degrees for considering the turn complete.
     * @property maxRotationSpeed Maximum rotation speed (0.0 to 1.0).
     */
    data class Config(
        var pingu: Pingu = Pingu(0.05, 0.0, 0.001),
        var tolerance: Double = 1.0,
        var maxRotationSpeed: Double = 0.5,
    )

    private var config = Config()
    private var pid =
        config.pingu.pidController.apply {
            setTolerance(toRadians(config.tolerance))
        }

    init {
        addRequirements(swerveDrive)
    }

    /**
     * Configures the turn to angle command using a DSL-style configuration block.
     *
     * ```kotlin
     * command.configure {
     *     kP = 0.05
     *     tolerance = 1.0
     * }
     * ```
     *
     * @param block Configuration block to apply settings.
     */
    fun configure(block: Config.() -> Unit) {
        config.apply(block)
        pid =
            config.pingu.pidController.apply {
                setTolerance(toRadians(config.tolerance))
            }
    }

    override fun initialize() {
        pid.reset()
    }

    override fun execute() {
        val currentAngle = swerveDrive.getGyroRotation()

        val rotationSpeed =
            pid
                .calculate(currentAngle.radians, targetAngle.radians)
                .coerceIn(-config.maxRotationSpeed, config.maxRotationSpeed)

        swerveDrive.drive(0.0, 0.0, rotationSpeed, false)
    }

    override fun end(interrupted: Boolean) = swerveDrive.stop()

    override fun isFinished(): Boolean = pid.atSetpoint()
}
