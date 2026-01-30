package xyz.malefic.frc.extension

import com.ctre.phoenix6.CANBus
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.wpilibj2.command.Command
import xyz.malefic.frc.swerve.SwerveDrive
import xyz.malefic.frc.swerve.SwerveModule
import xyz.malefic.frc.swerve.command.DriveToAngle
import xyz.malefic.frc.swerve.command.LockSwerve
import xyz.malefic.frc.swerve.command.ResetGyro
import xyz.malefic.frc.swerve.command.TeleopSwerve
import xyz.malefic.frc.swerve.command.TurnToAngle
import kotlin.math.sqrt

/**
 * Creates a swerve drive subsystem with a DSL for module configuration.
 *
 * ## Usage Example:
 * ```kotlin
 * val drive = swerveDrive(gyroId = 0) {
 *     modules(
 *         frontLeft = module(1, 2, 3, Rotation2d.fromDegrees(0.0)),
 *         frontRight = module(4, 5, 6, Rotation2d.fromDegrees(90.0)),
 *         backLeft = module(7, 8, 9, Rotation2d.fromDegrees(180.0)),
 *         backRight = module(10, 11, 12, Rotation2d.fromDegrees(270.0))
 *     )
 *     config {
 *         trackWidthMeters = 0.6
 *         wheelBaseMeters = 0.6
 *         maxLinearVelocityMPS = 4.5
 *     }
 * }
 * ```
 */
fun swerveDrive(
    gyroId: Int,
    canBus: CANBus = CANBus.roboRIO(),
    block: SwerveDriveBuilder.() -> Unit,
): SwerveDrive = SwerveDriveBuilder(gyroId, canBus).apply(block).build()

/**
 * Builder class for creating a [SwerveDrive] with DSL syntax.
 */
class SwerveDriveBuilder(
    private val gyroId: Int,
    private val canBus: CANBus,
) {
    private var frontLeft: SwerveModule? = null
    private var frontRight: SwerveModule? = null
    private var backLeft: SwerveModule? = null
    private var backRight: SwerveModule? = null
    private var driveConfig: (SwerveDrive.Config.() -> Unit)? = null

    /**
     * Sets the four swerve modules for the drive.
     */
    fun modules(
        frontLeft: SwerveModule,
        frontRight: SwerveModule,
        backLeft: SwerveModule,
        backRight: SwerveModule,
    ) {
        this.frontLeft = frontLeft
        this.frontRight = frontRight
        this.backLeft = backLeft
        this.backRight = backRight
    }

    /**
     * Configures the swerve drive parameters.
     */
    fun config(block: SwerveDrive.Config.() -> Unit) {
        driveConfig = block
    }

    /**
     * Builds the [SwerveDrive] instance.
     */
    fun build(): SwerveDrive {
        require(frontLeft != null) { "Front left module must be set" }
        require(frontRight != null) { "Front right module must be set" }
        require(backLeft != null) { "Back left module must be set" }
        require(backRight != null) { "Back right module must be set" }

        return SwerveDrive(
            gyroId,
            frontLeft!!,
            frontRight!!,
            backLeft!!,
            backRight!!,
            canBus,
        ).apply {
            driveConfig?.let { configure(it) }
        }
    }
}

/**
 * Creates a swerve module with DSL configuration.
 *
 * ## Usage Example:
 * ```kotlin
 * val module = swerveModule(
 *     driveId = 1,
 *     steerId = 2,
 *     encoderId = 3,
 *     offset = Rotation2d.fromDegrees(45.0)
 * ) {
 *     driveGearRatio = 6.75
 *     steerGearRatio = 150.0 / 7.0
 *     wheelDiameterMeters = 0.1016
 *     driveKP = 0.1
 *     steerKP = 100.0
 * }
 * ```
 */
fun swerveModule(
    driveId: Int,
    steerId: Int,
    encoderId: Int,
    offset: Rotation2d = Rotation2d(),
    block: SwerveModule.Config.() -> Unit = {},
): SwerveModule =
    SwerveModule(driveId, steerId, encoderId, offset).apply {
        configure(block)
    }

/**
 * Extension to create a simple module reference for builder DSL.
 */
fun module(
    driveId: Int,
    steerId: Int,
    encoderId: Int,
    offset: Rotation2d = Rotation2d(),
): SwerveModule = SwerveModule(driveId, steerId, encoderId, offset)

/**
 * Creates a teleop swerve command with DSL configuration.
 *
 * ## Usage Example:
 * ```kotlin
 * val teleopCommand = drive.teleopCommand(
 *     xSupplier = { controller.leftY },
 *     ySupplier = { controller.leftX },
 *     rotSupplier = { controller.rightX }
 * ) {
 *     deadband = 0.05
 *     inputExponent = 2.0
 *     enableSlewRateLimiting = true
 * }
 * ```
 */
fun SwerveDrive.teleopCommand(
    xSupplier: () -> Double,
    ySupplier: () -> Double,
    rotSupplier: () -> Double,
    fieldOriented: Boolean = true,
    block: TeleopSwerve.Config.() -> Unit = {},
): Command =
    TeleopSwerve(this, xSupplier, ySupplier, rotSupplier, fieldOriented).apply {
        configure(block)
    }

/**
 * Creates a lock swerve command.
 *
 * ## Usage Example:
 * ```kotlin
 * val lockCommand = drive.lockCommand()
 * controller.x().onTrue(lockCommand)
 * ```
 */
fun SwerveDrive.lockCommand(): Command = LockSwerve(this)

/**
 * Creates a reset gyro command.
 *
 * ## Usage Example:
 * ```kotlin
 * val resetCommand = drive.resetGyroCommand()
 * controller.start().onTrue(resetCommand)
 * ```
 */
fun SwerveDrive.resetGyroCommand(angle: Rotation2d = Rotation2d()): Command = ResetGyro(this, angle)

/**
 * Creates a drive to angle command with DSL configuration.
 *
 * ## Usage Example:
 * ```kotlin
 * val driveToAngle = drive.driveToAngleCommand(
 *     xSupplier = { controller.leftY },
 *     ySupplier = { controller.leftX },
 *     targetAngle = Rotation2d.fromDegrees(180.0)
 * ) {
 *     kP = 0.05
 *     tolerance = 2.0
 * }
 * ```
 */
fun SwerveDrive.driveToAngleCommand(
    xSupplier: () -> Double,
    ySupplier: () -> Double,
    targetAngle: Rotation2d,
    block: DriveToAngle.Config.() -> Unit = {},
): Command =
    DriveToAngle(this, xSupplier, ySupplier, targetAngle).apply {
        configure(block)
    }

/**
 * Creates a turn to angle command with DSL configuration.
 *
 * ## Usage Example:
 * ```kotlin
 * val turnCommand = drive.turnToAngleCommand(
 *     targetAngle = Rotation2d.fromDegrees(90.0)
 * ) {
 *     kP = 0.05
 *     tolerance = 1.0
 * }
 * ```
 */
fun SwerveDrive.turnToAngleCommand(
    targetAngle: Rotation2d,
    block: TurnToAngle.Config.() -> Unit = {},
): Command =
    TurnToAngle(this, targetAngle).apply {
        configure(block)
    }

/**
 * Extension to convert [Translation2d] to [ChassisSpeeds] with rotation.
 */
fun Translation2d.toChassisSpeeds(rotation: Double = 0.0): ChassisSpeeds = ChassisSpeeds(x, y, rotation)

/**
 * Extension to get the magnitude of chassis speeds.
 */
fun ChassisSpeeds.magnitude(): Double =
    sqrt(
        vxMetersPerSecond * vxMetersPerSecond +
            vyMetersPerSecond * vyMetersPerSecond,
    )

/**
 * Extension to scale chassis speeds by a factor.
 */
fun ChassisSpeeds.times(scale: Double): ChassisSpeeds =
    ChassisSpeeds(
        vxMetersPerSecond * scale,
        vyMetersPerSecond * scale,
        omegaRadiansPerSecond * scale,
    )

/**
 * Extension to add two chassis speeds together.
 */
operator fun ChassisSpeeds.plus(other: ChassisSpeeds): ChassisSpeeds =
    ChassisSpeeds(
        vxMetersPerSecond + other.vxMetersPerSecond,
        vyMetersPerSecond + other.vyMetersPerSecond,
        omegaRadiansPerSecond + other.omegaRadiansPerSecond,
    )
