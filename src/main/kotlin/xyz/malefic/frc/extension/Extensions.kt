package xyz.malefic.frc.extension

import com.ctre.phoenix6.configs.TalonFXConfiguration
import com.ctre.phoenix6.hardware.TalonFX
import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.apriltag.AprilTagFields
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Rotation3d
import edu.wpi.first.math.util.Units.inchesToMeters
import edu.wpi.first.math.util.Units.metersToInches
import edu.wpi.first.wpilibj.XboxController
import org.photonvision.EstimatedRobotPose
import org.photonvision.targeting.PhotonPipelineResult
import xyz.malefic.frc.pingu.Pingu
import xyz.malefic.frc.sub.PhotonModule
import java.util.Optional
import kotlin.math.abs

/**
 * Extension function for a list of [PhotonModule] objects to get the best [PhotonPipelineResult].
 *
 * This function iterates through each [PhotonModule] in the list, retrieves the latest result,
 * and checks if it has targets. If it does, it compares the pose ambiguity of the target
 * with the current best ambiguity. If the current target's ambiguity is lower, it updates
 * the best result.
 *
 * @receiver List<[PhotonModule]> The list of [PhotonModule] objects to search through.
 * @return List<Pair<[PhotonModule], [PhotonPipelineResult]>> The list of [PhotonModule] and [PhotonPipelineResult] pairs ordered by pose ambiguity.
 */
fun List<PhotonModule>.getDecentResultPairs(
    condition: (PhotonPipelineResult) -> Boolean = { it.hasTargets() },
): List<Pair<PhotonModule, PhotonPipelineResult>> =
    this
        .mapNotNull { module ->
            module.allUnreadResults
                .getOrNull(0)
                ?.takeIf { condition(it) }
                ?.let { module to it }
        }.sortedBy { it.second.bestTarget.poseAmbiguity }

/**
 * Extension function for a list of Pair<[PhotonModule], [PhotonPipelineResult]> objects to check if any have targets.
 *
 * This function iterates through each pair in the list and checks if the [PhotonPipelineResult] has targets.
 *
 * @receiver List<Pair<[PhotonModule], [PhotonPipelineResult]>> The list of pairs to check.
 * @return Boolean True if any pair has targets, false otherwise.
 */
fun List<Pair<PhotonModule, PhotonPipelineResult>>.hasTargets(): Boolean = this.any { it.second.hasTargets() }

/**
 * Extension function for a Pair of [PhotonModule] and [PhotonPipelineResult] to get estimated poses.
 *
 * This function sets the reference pose for the pose estimator of the [PhotonModule] and updates it
 * with the [PhotonPipelineResult]. If an [EstimatedRobotPose] is present, it adds it to the list of poses.
 *
 * @receiver Pair<[PhotonModule], [PhotonPipelineResult]> The pair of [PhotonModule] and [PhotonPipelineResult].
 * @param prevEstimatedRobotPose [Pose2d] The previous estimated robot pose to set as reference.
 * @return [EstimatedRobotPose]? The estimated robot pose, or null if not present.
 */
fun Pair<PhotonModule, PhotonPipelineResult>.getEstimatedPose(prevEstimatedRobotPose: Pose2d): EstimatedRobotPose? {
    first.poseEstimator.apply {
        setReferencePose(prevEstimatedRobotPose)
        return update(second).orElse(null)
    }
}

/**
 * Extension function for a Pair of [PhotonModule] and [PhotonPipelineResult] to update the standard deviations of the estimated robot pose.
 *
 * This function updates the estimated standard deviations of the robot pose using the provided [EstimatedRobotPose]
 * and the targets from the [PhotonPipelineResult].
 *
 * @receiver Pair<[PhotonModule], [PhotonPipelineResult]> The pair of [PhotonModule] and [PhotonPipelineResult].
 * @param estimatedRobotPose [Optional]<[EstimatedRobotPose]> The estimated robot pose to use for updating the standard deviations.
 */
fun Pair<PhotonModule, PhotonPipelineResult>.updateStdDev(estimatedRobotPose: Optional<EstimatedRobotPose>) {
    first.updateEstimatedStdDevs(estimatedRobotPose, second.getTargets())
}

/**
 * Extension function for a Pair of [PhotonModule] and [PhotonPipelineResult] to update the 3d standard deviations of the estimated robot pose.
 *
 * This function updates the estimated 3d standard deviations of the robot pose using the provided [EstimatedRobotPose]
 * and the targets from the [PhotonPipelineResult].
 *
 * @receiver Pair<[PhotonModule], [PhotonPipelineResult]> The pair of [PhotonModule] and [PhotonPipelineResult].
 * @param estimatedRobotPose [Optional]<[EstimatedRobotPose]> The estimated robot pose to use for updating the standard deviations.
 */
fun Pair<PhotonModule, PhotonPipelineResult>.updateStdDev3d(estimatedRobotPose: Optional<EstimatedRobotPose>) {
    first.updateEstimatedStdDevs3d(estimatedRobotPose, second.getTargets())
}

/**
 * Extension function to set the Pingu values of a [TalonFXConfiguration] using a [Pingu] object.
 *
 * @receiver [TalonFXConfiguration] The [TalonFX] configuration to set the values for.
 * @param pingu [Pingu] The [Pingu] object containing the PID values.
 */
fun TalonFXConfiguration.setPingu(pingu: Pingu) =
    pingu.apply {
        Slot0.kP = p
        Slot0.kI = i
        Slot0.kD = d
        Slot0.kV = v
        Slot0.kS = s
        Slot0.kG = g
    }

/**
 * Extension function to convert a [Rotation2d] to a [Rotation3d].
 *
 * @receiver [Rotation2d] The 2D rotation to convert.
 * @param yaw Double The yaw value for the 3D rotation.
 * @return [Rotation3d] The resulting 3D rotation.
 */
fun Rotation2d.to3d(yaw: Double) = Rotation3d(cos, sin, yaw)

/**
 * Operator function to add a yaw value to a [Rotation2d], resulting in a [Rotation3d].
 *
 * @receiver [Rotation2d] The 2D rotation to add the yaw to.
 * @param yaw Double The yaw value to add.
 * @return [Rotation3d] The resulting 3D rotation.
 */
operator fun Rotation2d.plus(yaw: Double) = Rotation3d(cos, sin, yaw)

/**
 * Extension property to get a new [Pose2d] rotated by 180 degrees from the current pose.
 *
 * @receiver [Pose2d] The original pose.
 * @return [Pose2d] The pose rotated by 180 degrees.
 */
val Pose2d.rotated180: Pose2d
    get() = Pose2d(this.translation, this.rotation.plus(Rotation2d.k180deg))

/**
 * Extension property to convert a value in inches to meters.
 *
 * @receiver [Double] The value in inches.
 * @return [Double] The value converted to meters.
 */
val Double.inchesToMeters: Double
    get() = inchesToMeters(this)

/**
 * Extension property to convert a value in meters to inches.
 *
 * @receiver [Double] The value in meters.
 * @return [Double] The value converted to inches.
 */
val Double.metersToInches: Double
    get() = metersToInches(this)

/**
 * Extension property to load the [AprilTagFieldLayout] for the given [AprilTagFields] enum value.
 *
 * @receiver [AprilTagFields] The enum value representing a specific AprilTag field.
 * @return [AprilTagFieldLayout] The loaded field layout for the specified field.
 */
val AprilTagFields.layout: AprilTagFieldLayout
    get() = AprilTagFieldLayout.loadField(this)

/**
 * Gets the position of the left stick based on the input from the controller.
 *
 * @receiver [XboxController] The controller.
 * @return [Pair]<Double, Double> The coordinate representing the position of the left stick. The first element is the x-coordinate, and
 * the second element is the y-coordinate.
 */
fun XboxController.leftStickPosition(
    xDeadzone: Double,
    yDeadzone: Double,
): Pair<Double, Double> {
    val x = if (abs(leftX) < xDeadzone) 0.0 else leftX
    val y = if (abs(leftY) < yDeadzone) 0.0 else leftY
    return Pair(x, y)
}
