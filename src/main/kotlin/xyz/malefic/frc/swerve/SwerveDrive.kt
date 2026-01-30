package xyz.malefic.frc.swerve

import com.ctre.phoenix6.CANBus
import com.ctre.phoenix6.hardware.Pigeon2
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.math.kinematics.SwerveDriveKinematics
import edu.wpi.first.math.kinematics.SwerveModulePosition
import edu.wpi.first.math.kinematics.SwerveModuleState
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import kotlin.math.PI

/**
 * Swerve drive subsystem managing four swerve modules and odometry.
 *
 * This subsystem handles:
 * - Swerve drive kinematics and control
 * - Pose estimation with vision integration
 * - Field-oriented and robot-oriented driving
 * - Gyroscope integration
 *
 * ## Usage Example:
 * ```kotlin
 * val drive = SwerveDrive(
 *     gyroId = 0,
 *     frontLeftModule = SwerveModule(1, 2, 3, Rotation2d.fromDegrees(0.0)),
 *     frontRightModule = SwerveModule(4, 5, 6, Rotation2d.fromDegrees(90.0)),
 *     backLeftModule = SwerveModule(7, 8, 9, Rotation2d.fromDegrees(180.0)),
 *     backRightModule = SwerveModule(10, 11, 12, Rotation2d.fromDegrees(270.0))
 * )
 *
 * drive.configure {
 *     trackWidthMeters = 0.6
 *     wheelBaseMeters = 0.6
 *     maxLinearVelocityMPS = 4.5
 *     maxAngularVelocityRadPS = 2.0 * PI
 * }
 *
 * // Drive field-oriented
 * drive.drive(
 *     xSpeed = 1.0,
 *     ySpeed = 0.5,
 *     rotSpeed = 0.2,
 *     fieldOriented = true
 * )
 * ```
 *
 * @property gyroId CAN ID for the Pigeon2 gyroscope.
 * @property frontLeftModule Front left swerve module.
 * @property frontRightModule Front right swerve module.
 * @property backLeftModule Back left swerve module.
 * @property backRightModule Back right swerve module.
 * @property canBus CAN bus name for the gyroscope (defaults to "rio").
 */
class SwerveDrive(
    gyroId: Int,
    private val frontLeftModule: SwerveModule,
    private val frontRightModule: SwerveModule,
    private val backLeftModule: SwerveModule,
    private val backRightModule: SwerveModule,
    canBus: CANBus = CANBus.roboRIO(),
) : SubsystemBase() {
    /**
     * Configuration for the swerve drive.
     *
     * @property trackWidthMeters Distance between left and right wheels in meters.
     * @property wheelBaseMeters Distance between front and back wheels in meters.
     * @property maxLinearVelocityMPS Maximum linear velocity in meters per second.
     * @property maxAngularVelocityRadPS Maximum angular velocity in radians per second.
     * @property driveDeadband Deadband for drive inputs (0.0 to 1.0).
     * @property rotationDeadband Deadband for rotation input (0.0 to 1.0).
     */
    data class Config(
        var trackWidthMeters: Double = 0.6,
        var wheelBaseMeters: Double = 0.6,
        var maxLinearVelocityMPS: Double = 4.5,
        var maxAngularVelocityRadPS: Double = 2.0 * PI,
        var driveDeadband: Double = 0.02,
        var rotationDeadband: Double = 0.02,
    )

    private var config = Config()

    private val gyro = Pigeon2(gyroId, canBus)

    private lateinit var kinematics: SwerveDriveKinematics
    private lateinit var poseEstimator: SwerveDrivePoseEstimator

    private val modules
        get() = arrayOf(frontLeftModule, frontRightModule, backLeftModule, backRightModule)

    init {
        gyro.reset()
        initializeKinematics()
    }

    private fun initializeKinematics() {
        val frontLeft = Translation2d(config.wheelBaseMeters / 2.0, config.trackWidthMeters / 2.0)
        val frontRight = Translation2d(config.wheelBaseMeters / 2.0, -config.trackWidthMeters / 2.0)
        val backLeft = Translation2d(-config.wheelBaseMeters / 2.0, config.trackWidthMeters / 2.0)
        val backRight = Translation2d(-config.wheelBaseMeters / 2.0, -config.trackWidthMeters / 2.0)

        kinematics = SwerveDriveKinematics(frontLeft, frontRight, backLeft, backRight)

        poseEstimator =
            SwerveDrivePoseEstimator(
                kinematics,
                getGyroRotation(),
                getModulePositions(),
                Pose2d(),
            )
    }

    /**
     * Configures the swerve drive using a DSL-style configuration block.
     *
     * ```kotlin
     * drive.configure {
     *     trackWidthMeters = 0.6
     *     wheelBaseMeters = 0.6
     *     maxLinearVelocityMPS = 4.5
     *     maxAngularVelocityRadPS = 2.0 * PI
     * }
     * ```
     *
     * @param block Configuration block to apply settings.
     */
    fun configure(block: Config.() -> Unit) {
        config.apply(block)
        initializeKinematics()
    }

    /**
     * Drives the robot with specified velocities.
     *
     * @param xSpeed Forward speed in m/s (positive = forward).
     * @param ySpeed Sideways speed in m/s (positive = left).
     * @param rotSpeed Rotational speed in rad/s (positive = counterclockwise).
     * @param fieldOriented Whether to drive field-oriented (true) or robot-oriented (false).
     * @param isOpenLoop Whether to use open loop control.
     */
    fun drive(
        xSpeed: Double,
        ySpeed: Double,
        rotSpeed: Double,
        fieldOriented: Boolean = true,
        isOpenLoop: Boolean = false,
    ) {
        val xSpeedFiltered = applyDeadband(xSpeed, config.driveDeadband) * config.maxLinearVelocityMPS
        val ySpeedFiltered = applyDeadband(ySpeed, config.driveDeadband) * config.maxLinearVelocityMPS
        val rotSpeedFiltered = applyDeadband(rotSpeed, config.rotationDeadband) * config.maxAngularVelocityRadPS

        val chassisSpeeds =
            if (fieldOriented) {
                ChassisSpeeds.fromFieldRelativeSpeeds(
                    xSpeedFiltered,
                    ySpeedFiltered,
                    rotSpeedFiltered,
                    getGyroRotation(),
                )
            } else {
                ChassisSpeeds(xSpeedFiltered, ySpeedFiltered, rotSpeedFiltered)
            }

        setModuleStates(kinematics.toSwerveModuleStates(chassisSpeeds), isOpenLoop)
    }

    /**
     * Drives the robot with chassis speeds.
     *
     * @param chassisSpeeds The desired chassis speeds.
     * @param isOpenLoop Whether to use open loop control.
     */
    fun drive(
        chassisSpeeds: ChassisSpeeds,
        isOpenLoop: Boolean = false,
    ) {
        setModuleStates(kinematics.toSwerveModuleStates(chassisSpeeds), isOpenLoop)
    }

    /**
     * Sets the desired states for all modules.
     *
     * @param desiredStates Array of desired states for each module (FL, FR, BL, BR).
     * @param isOpenLoop Whether to use open loop control.
     */
    fun setModuleStates(
        desiredStates: Array<SwerveModuleState>,
        isOpenLoop: Boolean = false,
    ) {
        SwerveDriveKinematics.desaturateWheelSpeeds(desiredStates, config.maxLinearVelocityMPS)
        frontLeftModule.setDesiredState(desiredStates[0], isOpenLoop)
        frontRightModule.setDesiredState(desiredStates[1], isOpenLoop)
        backLeftModule.setDesiredState(desiredStates[2], isOpenLoop)
        backRightModule.setDesiredState(desiredStates[3], isOpenLoop)
    }

    /**
     * Gets the current states of all modules.
     *
     * @return Array of current module states (FL, FR, BL, BR).
     */
    fun getModuleStates(): Array<SwerveModuleState> = modules.map { it.getState() }.toTypedArray()

    /**
     * Gets the current positions of all modules.
     *
     * @return Array of current module positions (FL, FR, BL, BR).
     */
    fun getModulePositions(): Array<SwerveModulePosition> = modules.map { it.getPosition() }.toTypedArray()

    /**
     * Gets the current rotation from the gyroscope.
     *
     * @return Current gyro rotation as [Rotation2d].
     */
    fun getGyroRotation(): Rotation2d = gyro.rotation2d

    /**
     * Gets the current estimated pose of the robot.
     *
     * @return Current estimated [Pose2d].
     */
    fun getPose(): Pose2d = poseEstimator.estimatedPosition

    /**
     * Resets the pose to a specific position and rotation.
     *
     * @param pose The new pose to reset to.
     */
    fun resetPose(pose: Pose2d) {
        poseEstimator.resetPosition(
            getGyroRotation(),
            getModulePositions(),
            pose,
        )
    }

    /**
     * Resets the gyro heading to zero.
     */
    fun resetGyro() {
        gyro.reset()
    }

    /**
     * Sets the gyro heading to a specific angle.
     *
     * @param angle The angle to set the gyro to.
     */
    fun setGyro(angle: Rotation2d) {
        gyro.setYaw(angle.degrees)
    }

    /**
     * Adds a vision measurement to the pose estimator.
     *
     * @param visionPose The pose measured by vision.
     * @param timestamp The timestamp of the vision measurement in seconds.
     */
    fun addVisionMeasurement(
        visionPose: Pose2d,
        timestamp: Double,
    ) {
        poseEstimator.addVisionMeasurement(visionPose, timestamp)
    }

    /**
     * Stops all modules.
     */
    fun stop() {
        modules.forEach { it.stop() }
    }

    /**
     * Gets the current chassis speeds.
     *
     * @return Current [ChassisSpeeds] of the robot.
     */
    fun getChassisSpeeds(): ChassisSpeeds = kinematics.toChassisSpeeds(*getModuleStates())

    /**
     * Creates a command to drive with joystick inputs.
     *
     * @param xSupplier Supplier for forward speed (-1.0 to 1.0).
     * @param ySupplier Supplier for sideways speed (-1.0 to 1.0).
     * @param rotSupplier Supplier for rotational speed (-1.0 to 1.0).
     * @param fieldOriented Whether to drive field-oriented.
     * @return A command that drives the robot.
     */
    fun driveCommand(
        xSupplier: () -> Double,
        ySupplier: () -> Double,
        rotSupplier: () -> Double,
        fieldOriented: Boolean = true,
    ): Command =
        run {
            drive(
                xSupplier(),
                ySupplier(),
                rotSupplier(),
                fieldOriented,
            )
        }

    /**
     * Creates a command to drive to a specific pose using PathPlanner.
     *
     * @param targetPose The target pose to drive to.
     * @return A command that drives to the target pose.
     */
    fun driveToPoseCommand(targetPose: Pose2d): Command =
        runOnce {
            // This is a placeholder for PathPlanner integration
            // In practice, you would use PathPlanner's auto builder here
        }

    override fun periodic() {
        poseEstimator.update(getGyroRotation(), getModulePositions())
    }

    private fun applyDeadband(
        value: Double,
        deadband: Double,
    ): Double {
        if (kotlin.math.abs(value) < deadband) return 0.0
        return if (value > 0) {
            (value - deadband) / (1.0 - deadband)
        } else {
            (value + deadband) / (1.0 - deadband)
        }
    }

    /**
     * Gets the maximum linear velocity in meters per second.
     *
     * @return Maximum linear velocity.
     */
    fun getMaxLinearVelocityMPS(): Double = config.maxLinearVelocityMPS

    /**
     * Gets the maximum angular velocity in radians per second.
     *
     * @return Maximum angular velocity.
     */
    fun getMaxAngularVelocityRadPS(): Double = config.maxAngularVelocityRadPS
}
