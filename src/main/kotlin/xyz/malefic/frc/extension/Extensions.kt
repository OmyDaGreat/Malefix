package xyz.malefic.frc.extension

import com.ctre.phoenix6.configs.TalonFXConfiguration
import com.ctre.phoenix6.hardware.TalonFX
import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.apriltag.AprilTagFields
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Pose3d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Rotation3d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.math.util.Units.inchesToMeters
import edu.wpi.first.math.util.Units.metersToInches
import edu.wpi.first.units.Units
import edu.wpi.first.units.measure.Distance
import edu.wpi.first.wpilibj.XboxController
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.CommandScheduler
import org.photonvision.EstimatedRobotPose
import org.photonvision.targeting.PhotonPipelineResult
import xyz.malefic.frc.pingu.control.Pingu
import xyz.malefic.frc.vision.PhotonModule
import kotlin.math.abs

/**
 * Extension function for a list of [PhotonModule] objects to get the best [PhotonPipelineResult].
 *
 * This function iterates through each [PhotonModule] in the list, retrieves the latest result,
 * and checks if it has targets. If it does, it compares the pose ambiguity of the target
 * with the current best ambiguity. If the current target's ambiguity is lower, it updates
 * the best result.
 *
 * @receiver List of [PhotonModule] objects to search through.
 * @return List of [Pair] of [PhotonModule] and [PhotonPipelineResult] ordered by pose ambiguity.
 */
fun List<PhotonModule>.getDecentResultPairs(
    condition: (PhotonPipelineResult) -> Boolean = { it.hasTargets() },
): List<Pair<PhotonModule, PhotonPipelineResult>> =
    this
        .mapNotNull { module ->
            module
                .getAllUnreadResults()
                .getOrNull(0)
                ?.takeIf { condition(it) }
                ?.let { module to it }
        }.sortedBy { it.second.bestTarget.poseAmbiguity }

/**
 * Extension function for a list of [Pair] of [PhotonModule] and [PhotonPipelineResult] to check if any have targets.
 *
 * This function iterates through each pair in the list and checks if the [PhotonPipelineResult] has targets.
 *
 * @receiver List of [Pair] of [PhotonModule] and [PhotonPipelineResult] to check.
 * @return [Boolean] True if any pair has targets, false otherwise.
 */
fun List<Pair<PhotonModule, PhotonPipelineResult>>.hasTargets(): Boolean = this.any { it.second.hasTargets() }

/**
 * Extension function for a Pair of [PhotonModule] and [PhotonPipelineResult] to get estimated poses.
 *
 * This function estimates the robot pose using the closest to reference pose strategy.
 *
 * @receiver Pair of [PhotonModule] and [PhotonPipelineResult] The pair of [PhotonModule] and [PhotonPipelineResult].
 * @param prevEstimatedRobotPose [Pose2d] The previous estimated robot pose to set as reference.
 * @return [EstimatedRobotPose]? The estimated robot pose, or null if not present.
 */
fun Pair<PhotonModule, PhotonPipelineResult>.getEstimatedPose(prevEstimatedRobotPose: Pose2d): EstimatedRobotPose? {
    val referencePose3d = Pose3d(prevEstimatedRobotPose)

    // Try closest to reference first
    var estimatedPose = first.poseEstimator.estimateClosestToReferencePose(second, referencePose3d).orElse(null)

    // Fallback to multi-tag coprocessor
    if (estimatedPose == null) {
        estimatedPose = first.poseEstimator.estimateCoprocMultiTagPose(second).orElse(null)
    }

    // Final fallback to lowest ambiguity
    if (estimatedPose == null) {
        estimatedPose = first.poseEstimator.estimateLowestAmbiguityPose(second).orElse(null)
    }

    return estimatedPose
}

/**
 * Extension function for a Pair of [PhotonModule] and [PhotonPipelineResult] to update the standard deviations of the estimated robot pose.
 *
 * This function updates the estimated standard deviations of the robot pose using the provided [EstimatedRobotPose]
 * and the targets from the [PhotonPipelineResult].
 *
 * @receiver Pair of [PhotonModule] and [PhotonPipelineResult] The pair of [PhotonModule] and [PhotonPipelineResult].
 * @param estimatedRobotPose [EstimatedRobotPose]? The estimated robot pose to use for updating the standard deviations.
 */
fun Pair<PhotonModule, PhotonPipelineResult>.updateStdDev(estimatedRobotPose: EstimatedRobotPose?) {
    first.updateEstimatedStdDevs(second, estimatedRobotPose)
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
 * Extension function to get the [Pingu] values from a [TalonFXConfiguration].
 *
 * @return [Pingu] The [Pingu] object containing the PID values.
 */
fun TalonFXConfiguration.getPingu(): Pingu =
    Pingu(
        p = Slot0.kP,
        i = Slot0.kI,
        d = Slot0.kD,
        v = Slot0.kV,
        s = Slot0.kS,
        g = Slot0.kG,
    )

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

/**
 * Gets the position of the right stick based on the input from the controller.
 *
 * @receiver [XboxController] The controller.
 * @return [Pair]<Double, Double> The coordinate representing the position of the right stick. The first element is the x-coordinate, and
 * the second element is the y-coordinate.
 */
fun XboxController.rightStickPosition(
    xDeadzone: Double,
    yDeadzone: Double,
): Pair<Double, Double> {
    val x = if (abs(rightX) < xDeadzone) 0.0 else rightX
    val y = if (abs(rightY) < yDeadzone) 0.0 else rightY
    return Pair(x, y)
}

/**
 * Schedule this command when the provided [condition] is true.
 *
 * @param condition If true, the command will be scheduled using the global [CommandScheduler].
 */
fun Command.scheduleIf(condition: Boolean) {
    if (condition) {
        this()
    }
}

/**
 * Operator invoke implementation for [Command].
 *
 * Schedules this command using the global [CommandScheduler]. Allows calling
 * the command with a function-call style: `myCommand()`.
 */
operator fun Command.invoke() = CommandScheduler.getInstance().schedule(this)

/**
 * Calculates the distance from a robot pose to a specific AprilTag.
 *
 * @receiver [Pose2d] The robot's current pose.
 * @param tagId The AprilTag ID to calculate distance to.
 * @param fieldLayout The field layout containing tag positions.
 * @return [Distance] The distance to the tag, or null if the tag doesn't exist.
 */
fun Pose2d.distanceToTag(
    tagId: Int,
    fieldLayout: AprilTagFieldLayout = AprilTagFields.kDefaultField.layout,
): Distance? {
    val tagPoseOptional = fieldLayout.getTagPose(tagId)
    if (tagPoseOptional.isEmpty) return null

    val tagPose = tagPoseOptional.get().toPose2d()
    val distance = this.translation.getDistance(tagPose.translation)
    return Units.Meters.of(distance)
}

/**
 * Calculates the distance from a robot pose to the closest AprilTag.
 *
 * @receiver [Pose2d] The robot's current pose.
 * @param fieldLayout The field layout containing tag positions.
 * @return [Distance] The distance to the closest tag, or null if no tags exist.
 */
fun Pose2d.distanceToClosestTag(fieldLayout: AprilTagFieldLayout = AprilTagFields.kDefaultField.layout): Distance? {
    val tags = fieldLayout.tags
    if (tags.isEmpty()) return null

    var closestDistance = Double.MAX_VALUE
    for (tag in tags) {
        val tagPose = tag.pose.toPose2d()
        val distance = this.translation.getDistance(tagPose.translation)
        if (distance < closestDistance) {
            closestDistance = distance
        }
    }

    return if (closestDistance == Double.MAX_VALUE) null else Units.Meters.of(closestDistance)
}

/**
 * Calculates the distance from a robot pose to an arbitrary field location.
 *
 * @receiver [Pose2d] The robot's current pose.
 * @param targetLocation The target location on the field.
 * @return [Distance] The distance to the target location.
 */
fun Pose2d.distanceTo(targetLocation: Translation2d): Distance {
    val distance = this.translation.getDistance(targetLocation)
    return Units.Meters.of(distance)
}

/**
 * Calculates the distance from a robot pose to another pose on the field.
 *
 * @receiver [Pose2d] The robot's current pose.
 * @param targetPose The target pose on the field.
 * @return [Distance] The distance to the target pose.
 */
fun Pose2d.distanceTo(targetPose: Pose2d): Distance {
    val distance = this.translation.getDistance(targetPose.translation)
    return Units.Meters.of(distance)
}

/**
 * Finds the closest AprilTag to the robot's current pose.
 *
 * @receiver [Pose2d] The robot's current pose.
 * @param fieldLayout The field layout containing tag positions.
 * @return Pair of tag ID and distance, or null if no tags exist.
 */
fun Pose2d.findClosestTag(fieldLayout: AprilTagFieldLayout): Pair<Int, Distance>? {
    val tags = fieldLayout.tags
    if (tags.isEmpty()) return null

    var closestDistance = Double.MAX_VALUE
    var closestTagId = -1

    for (tag in tags) {
        val tagPose = tag.pose.toPose2d()
        val distance = this.translation.getDistance(tagPose.translation)
        if (distance < closestDistance) {
            closestDistance = distance
            closestTagId = tag.ID
        }
    }

    return if (closestTagId == -1) null else Pair(closestTagId, Units.Meters.of(closestDistance))
}
