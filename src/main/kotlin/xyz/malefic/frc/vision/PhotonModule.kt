package xyz.malefic.frc.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.Matrix
import edu.wpi.first.math.VecBuilder
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Pose3d
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.math.numbers.N1
import edu.wpi.first.math.numbers.N3
import org.photonvision.EstimatedRobotPose
import org.photonvision.PhotonCamera
import org.photonvision.PhotonPoseEstimator
import org.photonvision.targeting.PhotonPipelineResult
import org.photonvision.targeting.PhotonTrackedTarget

/**
 * The [PhotonModule] class represents a single PhotonVision camera setup with its associated [PhotonPoseEstimator] and position information.
 *
 * Integrates with WPILib 2026 vision estimation APIs for improved pose accuracy.
 * Implements the [Camera] interface for unified vision system integration.
 *
 * ## Usage Example:
 * ```kotlin
 * val camera = PhotonModule(
 *     cameraName = "frontCamera",
 *     cameraPos = Transform3d(Translation3d(0.3, 0.0, 0.2), Rotation3d(0.0, Math.toRadians(-15.0), 0.0)),
 *     fieldLayout = AprilTagFields.k2024Crescendo.loadAprilTagLayoutField()
 * )
 *
 * // Get latest measurement
 * val measurement = camera.getLatestMeasurement()
 * measurement?.let {
 *     swerveDrive.addVisionMeasurement(it.pose, it.timestampSeconds, it.stdDevs)
 * }
 * ```
 *
 * @property cameraName The name of the camera.
 * @property cameraPos The position of the camera relative to robot center as a [Transform3d].
 * @property fieldLayout The [AprilTagFieldLayout] for the camera.
 */
class PhotonModule(
    private val cameraName: String,
    val cameraPos: Transform3d,
    fieldLayout: AprilTagFieldLayout,
) : Camera {
    companion object {
        private val DEFAULT_SINGLE_TARGET_STD_DEVS = VecBuilder.fill(0.7, 0.7, 0.5)
        private val DEFAULT_MULTI_TARGET_STD_DEVS = VecBuilder.fill(0.3, 0.3, 0.2)

        private const val DISTANCE_THRESHOLD = 4.0
        private const val DISTANCE_SCALE_FACTOR = 30.0
        private const val MAX_AMBIGUITY = 0.2
        private const val MAX_STD_DEV = 10.0
    }

    /**
     * The [PhotonCamera] instance for this module.
     */
    private val photonCamera = PhotonCamera(cameraName)

    /**
     * The [PhotonPoseEstimator] instance for estimating the robot's pose.
     *
     * Internal use for legacy extensions.
     */
    internal val poseEstimator =
        PhotonPoseEstimator(
            fieldLayout,
            cameraPos,
        )

    /**
     * The current standard deviations for pose estimation as a [Matrix].
     */
    var currentStdDevs: Matrix<N3, N1> = DEFAULT_SINGLE_TARGET_STD_DEVS
        private set

    // Camera interface implementation
    override val name: String get() = cameraName

    override fun hasTarget(): Boolean {
        val results = photonCamera.getAllUnreadResults()
        return results.isNotEmpty() && results.last().hasTargets()
    }

    override fun getTargetHorizontalOffset(): Double? {
        val results = photonCamera.getAllUnreadResults()
        if (results.isEmpty()) return null
        val result = results.last()
        return if (result.hasTargets()) result.bestTarget.yaw else null
    }

    override fun getTargetVerticalOffset(): Double? {
        val results = photonCamera.getAllUnreadResults()
        if (results.isEmpty()) return null
        val result = results.last()
        return if (result.hasTargets()) result.bestTarget.pitch else null
    }

    override fun getLatestMeasurement(referencePose: Pose2d?): VisionMeasurement? {
        val estimatedPose =
            if (referencePose != null) {
                getEstimatedGlobalPose(referencePose)
            } else {
                getEstimatedGlobalPose()
            }

        if (estimatedPose == null) return null

        val result = getLatestResult()
        updateEstimatedStdDevs(result, estimatedPose)

        return VisionMeasurement(
            pose = estimatedPose.estimatedPose.toPose2d(),
            timestampSeconds = estimatedPose.timestampSeconds,
            targetsUsed = estimatedPose.targetsUsed.size,
            stdDevs = currentStdDevs,
            averageTagDistance = calculateAverageDistance(estimatedPose),
            ambiguity = estimatedPose.targetsUsed.minOfOrNull { it.poseAmbiguity } ?: 0.0,
        )
    }

    override fun getAllUnreadMeasurements(): List<VisionMeasurement> =
        getAllUnreadResults().mapNotNull { result ->
            if (!result.hasTargets()) return@mapNotNull null

            // Try multi-tag coprocessor first
            var estimatedPose = poseEstimator.estimateCoprocMultiTagPose(result).orElse(null)

            // Fallback to lowest ambiguity
            if (estimatedPose == null) {
                estimatedPose = poseEstimator.estimateLowestAmbiguityPose(result).orElse(null)
            }

            if (estimatedPose == null) return@mapNotNull null

            updateEstimatedStdDevs(result, estimatedPose)

            VisionMeasurement(
                pose = estimatedPose.estimatedPose.toPose2d(),
                timestampSeconds = estimatedPose.timestampSeconds,
                targetsUsed = estimatedPose.targetsUsed.size,
                stdDevs = currentStdDevs,
                averageTagDistance = calculateAverageDistance(estimatedPose),
                ambiguity = estimatedPose.targetsUsed.minOfOrNull { it.poseAmbiguity } ?: 0.0,
            )
        }

    /**
     * Gets the latest [PhotonPipelineResult] from the camera.
     *
     * @return The latest pipeline result, or an empty result if none available.
     */
    fun getLatestResult(): PhotonPipelineResult {
        val results = photonCamera.getAllUnreadResults()
        return if (results.isNotEmpty()) results.last() else PhotonPipelineResult()
    }

    /**
     * Gets all unread [PhotonPipelineResult]s from the [PhotonCamera].
     *
     * @return List of all unread results.
     */
    fun getAllUnreadResults(): List<PhotonPipelineResult> = photonCamera.allUnreadResults

    /**
     * Gets the estimated global pose from the pose estimator.
     *
     * Uses coprocessor multi-tag estimation with lowest ambiguity fallback.
     *
     * @return The estimated robot pose, or null if no valid pose is available.
     */
    private fun getEstimatedGlobalPose(): EstimatedRobotPose? {
        val result = getLatestResult()

        // Try multi-tag coprocessor estimation first
        var estimatedPose = poseEstimator.estimateCoprocMultiTagPose(result).orElse(null)

        // Fallback to lowest ambiguity if multi-tag fails
        if (estimatedPose == null) {
            estimatedPose = poseEstimator.estimateLowestAmbiguityPose(result).orElse(null)
        }

        return estimatedPose
    }

    /**
     * Gets the estimated global pose with a reference robot pose for improved accuracy.
     *
     * Uses closest to reference pose estimation when available, with fallback strategies.
     *
     * @param referencePose The reference robot pose to use.
     * @return The estimated robot pose, or null if no valid pose is available.
     */
    private fun getEstimatedGlobalPose(referencePose: Pose2d): EstimatedRobotPose? {
        val result = getLatestResult()
        val referencePose3d = Pose3d(referencePose)

        // Try closest to reference pose first (best when we have a good reference)
        var estimatedPose = poseEstimator.estimateClosestToReferencePose(result, referencePose3d).orElse(null)

        // Fallback to multi-tag coprocessor if reference-based fails
        if (estimatedPose == null) {
            estimatedPose = poseEstimator.estimateCoprocMultiTagPose(result).orElse(null)
        }

        // Final fallback to lowest ambiguity
        if (estimatedPose == null) {
            estimatedPose = poseEstimator.estimateLowestAmbiguityPose(result).orElse(null)
        }

        return estimatedPose
    }

    /**
     * Updates the estimated standard deviations based on the provided result.
     *
     * @param result The pipeline result to use for updating standard deviations.
     * @param estimatedPose The estimated robot pose.
     * @param singleTargetVector The standard deviations for a single target.
     * @param multiTargetVector The standard deviations for multiple targets.
     */
    fun updateEstimatedStdDevs(
        result: PhotonPipelineResult,
        estimatedPose: EstimatedRobotPose?,
        singleTargetVector: Matrix<N3, N1> = DEFAULT_SINGLE_TARGET_STD_DEVS,
        multiTargetVector: Matrix<N3, N1> = DEFAULT_MULTI_TARGET_STD_DEVS,
    ) {
        if (estimatedPose == null || !result.hasTargets()) {
            currentStdDevs = singleTargetVector
            return
        }

        val targets = result.targets
        val tagInfo = calculateTagInfo(estimatedPose, targets)

        // Reject poses with high ambiguity on single targets
        if (tagInfo.numTags == 1 && tagInfo.maxAmbiguity > MAX_AMBIGUITY) {
            currentStdDevs = VecBuilder.fill(MAX_STD_DEV, MAX_STD_DEV, MAX_STD_DEV)
            return
        }

        currentStdDevs =
            calculateStdDevs(
                tagInfo.numTags,
                tagInfo.avgDistance,
                singleTargetVector,
                multiTargetVector,
            )
    }

    /**
     * Helper class to store tag information.
     *
     * @property numTags The number of tags.
     * @property avgDistance The average distance of the tags.
     * @property maxAmbiguity The maximum ambiguity of the targets.
     */
    private data class TagInfo(
        val numTags: Int,
        val avgDistance: Double,
        val maxAmbiguity: Double,
    )

    /**
     * Calculate the number of visible tags and their average distance.
     *
     * @param estimatedPose The [EstimatedRobotPose] to use for updating the standard deviations.
     * @param targets The list of [PhotonTrackedTarget]s to use for updating the standard deviations.
     * @return [TagInfo] The number of tags, their average distance, and max ambiguity.
     */
    private fun calculateTagInfo(
        estimatedPose: EstimatedRobotPose,
        targets: List<PhotonTrackedTarget>,
    ): TagInfo {
        var numTags = 0
        var totalDistance = 0.0
        var maxAmbiguity = 0.0

        for (target in targets) {
            val tagPoseOptional = poseEstimator.fieldTags.getTagPose(target.fiducialId)
            if (tagPoseOptional.isEmpty) continue

            numTags++
            val tagPose = tagPoseOptional.get()
            val estimatedTranslation = estimatedPose.estimatedPose
            totalDistance += tagPose.toPose2d().translation.getDistance(estimatedTranslation.toPose2d().translation)
            maxAmbiguity = maxOf(maxAmbiguity, target.poseAmbiguity)
        }

        val avgDistance = if (numTags > 0) totalDistance / numTags else 0.0
        return TagInfo(numTags, avgDistance, maxAmbiguity)
    }

    /**
     * Calculate standard deviations based on tag information.
     *
     * @param numTags The number of tags.
     * @param avgDistance The average distance of the tags.
     * @param singleTargetVector The standard deviations for a single target.
     * @param multiTargetVector The standard deviations for multiple targets.
     * @return The calculated standard deviations.
     */
    private fun calculateStdDevs(
        numTags: Int,
        avgDistance: Double,
        singleTargetVector: Matrix<N3, N1>,
        multiTargetVector: Matrix<N3, N1>,
    ): Matrix<N3, N1> {
        if (numTags == 0) {
            return singleTargetVector
        }

        val baseStdDevs = if (numTags > 1) multiTargetVector else singleTargetVector

        // Scale by distance: farther targets have higher uncertainty
        val distanceMultiplier =
            if (numTags == 1 && avgDistance > DISTANCE_THRESHOLD) {
                // For single far targets, increase uncertainty significantly
                1.0 + (avgDistance * avgDistance / DISTANCE_SCALE_FACTOR)
            } else {
                // For multi-tag or close single targets, scale more conservatively
                1.0 + (avgDistance * avgDistance / (DISTANCE_SCALE_FACTOR * 2.0))
            }

        return baseStdDevs.times(distanceMultiplier)
    }

    /**
     * Calculates the average distance to all visible tags.
     *
     * @param estimatedPose The estimated robot pose.
     * @return Average distance in meters.
     */
    private fun calculateAverageDistance(estimatedPose: EstimatedRobotPose): Double {
        if (estimatedPose.targetsUsed.isEmpty()) return 0.0

        var totalDistance = 0.0
        for (target in estimatedPose.targetsUsed) {
            val tagPoseOptional = poseEstimator.fieldTags.getTagPose(target.fiducialId)
            if (tagPoseOptional.isEmpty) continue

            val tagPose = tagPoseOptional.get()
            val estimatedTranslation = estimatedPose.estimatedPose
            totalDistance += tagPose.toPose2d().translation.getDistance(estimatedTranslation.toPose2d().translation)
        }

        return totalDistance / estimatedPose.targetsUsed.size
    }
}
