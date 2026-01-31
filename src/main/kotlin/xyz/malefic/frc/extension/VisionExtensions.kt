package xyz.malefic.frc.extension

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Pose3d
import org.photonvision.EstimatedRobotPose
import org.photonvision.targeting.PhotonPipelineResult
import xyz.malefic.frc.vision.PhotonModule

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
