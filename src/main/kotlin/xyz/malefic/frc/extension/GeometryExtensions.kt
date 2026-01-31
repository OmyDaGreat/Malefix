package xyz.malefic.frc.extension

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.apriltag.AprilTagFields
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Rotation3d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.units.Units
import edu.wpi.first.units.measure.Distance

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

/**
 * Extension property to load the [AprilTagFieldLayout] for the given [AprilTagFields] enum value.
 *
 * @receiver [AprilTagFields] The enum value representing a specific AprilTag field.
 * @return [AprilTagFieldLayout] The loaded field layout for the specified field.
 */
val AprilTagFields.layout: AprilTagFieldLayout
    get() = AprilTagFieldLayout.loadField(this)
