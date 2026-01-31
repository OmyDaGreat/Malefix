package xyz.malefic.frc.vision.command

import co.touchlab.kermit.Logger
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj2.command.Command
import xyz.malefic.frc.swerve.SwerveDrive
import xyz.malefic.frc.vision.VisionSystem
import kotlin.math.abs

/**
 * Command to reset the robot's pose estimation using vision measurements.
 *
 * This command uses the best available vision pose estimate to reset the robot's
 * odometry. Useful for re-localizing the robot at the start of autonomous or after
 * significant pose drift.
 *
 * ## Usage Example:
 * ```kotlin
 * val resetPoseCommand = ResetPoseFromVision(
 *     swerveDrive = drive,
 *     visionSystem = visionSystem
 * ).apply {
 *     configure {
 *         minTargetsRequired = 2
 *         requireStableEstimate = true
 *         stableReadingsRequired = 5
 *     }
 * }
 *
 * // Use in autonomous initialization
 * SequentialCommandGroup(
 *     resetPoseCommand,
 *     followTrajectoryCommand
 * ).schedule()
 * ```
 *
 * @property swerveDrive The swerve drive subsystem to update.
 * @property visionSystem The vision system to get pose estimates from.
 */
class ResetPoseFromVision(
    private val swerveDrive: SwerveDrive,
    private val visionSystem: VisionSystem,
) : Command() {
    /**
     * Configuration for pose reset.
     *
     * @property minTargetsRequired Minimum number of AprilTags required for pose reset.
     * @property requireStableEstimate Whether to require multiple stable readings before resetting.
     * @property stableReadingsRequired Number of consecutive stable readings required.
     * @property maxPoseVarianceMeters Maximum allowed variance between consecutive readings.
     * @property maxPoseVarianceRotation Maximum allowed rotation variance between consecutive readings (in degrees).
     * @property timeoutSeconds Maximum time to wait for a valid pose estimate.
     */
    data class Config(
        var minTargetsRequired: Int = 1,
        var requireStableEstimate: Boolean = false,
        var stableReadingsRequired: Int = 3,
        var maxPoseVarianceMeters: Double = 0.1,
        var maxPoseVarianceRotation: Double = 5.0,
        var timeoutSeconds: Double = 2.0,
    )

    private var config = Config()
    private var stableReadings = 0
    private var lastPose: edu.wpi.first.math.geometry.Pose2d? = null
    private var startTime: Double = 0.0

    init {
        addRequirements(swerveDrive)
    }

    /**
     * Configures the reset command using a DSL-style configuration block.
     *
     * ```kotlin
     * resetCommand.configure {
     *     minTargetsRequired = 2
     *     requireStableEstimate = true
     * }
     * ```
     *
     * @param block Configuration block to apply settings.
     * @return This command for method chaining.
     */
    fun configure(block: Config.() -> Unit): ResetPoseFromVision =
        apply {
            config.apply(block)
        }

    override fun initialize() {
        stableReadings = 0
        lastPose = null
        startTime = Timer.getFPGATimestamp()
    }

    override fun execute() {
        val visionMeasurement = visionSystem.getBestVisionMeasurement() ?: return

        // Check if enough targets are visible
        if (visionMeasurement.targetsUsed < config.minTargetsRequired) {
            return
        }

        val currentPose = visionMeasurement.pose

        if (!config.requireStableEstimate) {
            // Directly reset pose without stability check
            swerveDrive.resetPose(currentPose)
            stableReadings = config.stableReadingsRequired // Mark as complete
            return
        }

        // Check stability with previous reading
        if (lastPose != null) {
            val distance = currentPose.translation.getDistance(lastPose!!.translation)
            val rotationDiff = abs(currentPose.rotation.degrees - lastPose!!.rotation.degrees)

            if (distance < config.maxPoseVarianceMeters && rotationDiff < config.maxPoseVarianceRotation) {
                stableReadings++
            } else {
                stableReadings = 0
            }
        }

        lastPose = currentPose

        // Reset pose if we have enough stable readings
        if (stableReadings >= config.stableReadingsRequired) {
            swerveDrive.resetPose(currentPose)
        }
    }

    override fun end(interrupted: Boolean) {
        if (interrupted) {
            Logger.e("Vision") { "ResetPoseFromVision interrupted before completing" }
        } else {
            Logger.i("Vision") { "ResetPoseFromVision completed successfully" }
        }
    }

    override fun isFinished(): Boolean {
        val elapsedTime = Timer.getFPGATimestamp() - startTime

        // Finish if we have enough stable readings
        if (stableReadings >= config.stableReadingsRequired) {
            return true
        }

        // Timeout if taking too long
        if (elapsedTime > config.timeoutSeconds) {
            Logger.e("Vision") { "ResetPoseFromVision timed out" }
            return true
        }

        return false
    }
}
