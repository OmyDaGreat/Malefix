package xyz.malefic.frc.sub

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.Matrix
import edu.wpi.first.math.VecBuilder
import edu.wpi.first.math.geometry.Pose3d
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.math.numbers.N1
import edu.wpi.first.math.numbers.N3
import edu.wpi.first.math.numbers.N4
import org.photonvision.EstimatedRobotPose
import org.photonvision.PhotonCamera
import org.photonvision.PhotonPoseEstimator
import org.photonvision.PhotonPoseEstimator.PoseStrategy.LOWEST_AMBIGUITY
import org.photonvision.PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR
import org.photonvision.targeting.PhotonPipelineResult
import org.photonvision.targeting.PhotonTrackedTarget
import java.util.Optional
import kotlin.Double.Companion.MAX_VALUE
import kotlin.math.sqrt

/**
 * The PhotonModule class represents a single Photonvision camera setup with its associated pose
 * estimator and position information.
 *
 * @property cameraName The name of the camera.
 * @property cameraPos The position of the camera.
 * @property fieldLayout The field layout for the camera.
 */
class PhotonModule(
    val cameraName: String,
    val cameraPos: Transform3d,
    fieldLayout: AprilTagFieldLayout,
) {
    companion object {
        // Constants for standard deviation calculations
        private val DEFAULT_SINGLE_TARGET_STD_DEVS_2D = VecBuilder.fill(0.08, 0.08, 0.05)
        private val DEFAULT_MULTI_TARGET_STD_DEVS_2D = VecBuilder.fill(0.05, 0.05, 0.03)
        private val DEFAULT_SINGLE_TARGET_STD_DEVS_3D = VecBuilder.fill(0.08, 0.08, 0.08, 0.05)
        private val DEFAULT_MULTI_TARGET_STD_DEVS_3D = VecBuilder.fill(0.05, 0.05, 0.05, 0.03)

        private const val DISTANCE_THRESHOLD = 4.0
        private const val DISTANCE_SCALE_FACTOR = 30.0
    }

    /**
     * The PhotonCamera instance for this module.
     */
    val camera = PhotonCamera(cameraName)

    /**
     * The PhotonPoseEstimator instance for estimating the robot's pose.
     */
    val poseEstimator =
        PhotonPoseEstimator(
            fieldLayout,
            MULTI_TAG_PNP_ON_COPROCESSOR,
            cameraPos,
        ).apply {
            setMultiTagFallbackStrategy(LOWEST_AMBIGUITY)
        }

    /**
     * The current standard deviations for 2D pose estimation.
     */
    var currentStdDevs: Matrix<N3, N1>? = null
        private set

    /**
     * The current standard deviations for 3D pose estimation.
     */
    var currentStdDevs3d: Matrix<N4, N1>? = null
        private set

    /**
     * Retrieves all unread results from the PhotonCamera.
     */
    val allUnreadResults: MutableList<PhotonPipelineResult>
        get() = camera.allUnreadResults

    /**
     * Updates the estimated standard deviations based on the provided estimated pose and targets.
     *
     * @param estimatedPose The estimated robot pose to use for updating the standard deviations.
     * @param targets The list of tracked targets to use for updating the standard deviations.
     * @param singleTargetVector The standard deviations for a single target.
     * @param multiTargetVector The standard deviations for multiple targets.
     */
    fun updateEstimatedStdDevs(
        estimatedPose: Optional<EstimatedRobotPose>,
        targets: List<PhotonTrackedTarget>,
        singleTargetVector: Matrix<N3, N1> = DEFAULT_SINGLE_TARGET_STD_DEVS_2D,
        multiTargetVector: Matrix<N3, N1> = DEFAULT_MULTI_TARGET_STD_DEVS_2D,
    ) {
        if (!estimatedPose.isPresent) {
            currentStdDevs = singleTargetVector
            return
        }

        val tagInfo =
            calculateTagInfo(estimatedPose, targets) { tagPose, estimatedTranslation ->
                tagPose.toPose2d().translation.getDistance(estimatedTranslation.toPose2d().translation)
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
     * Updates the estimated 3D standard deviations.
     *
     * @param estimatedPose The estimated robot pose to use for updating the standard deviations.
     * @param targets The list of tracked targets to use for updating the standard deviations.
     * @param singleTargetVector The standard deviations for a single target.
     * @param multiTargetVector The standard deviations for multiple targets.
     */
    fun updateEstimatedStdDevs3d(
        estimatedPose: Optional<EstimatedRobotPose>,
        targets: List<PhotonTrackedTarget>,
        singleTargetVector: Matrix<N4, N1> = DEFAULT_SINGLE_TARGET_STD_DEVS_3D,
        multiTargetVector: Matrix<N4, N1> = DEFAULT_MULTI_TARGET_STD_DEVS_3D,
    ) {
        if (!estimatedPose.isPresent) {
            currentStdDevs3d = singleTargetVector
            return
        }

        val tagInfo =
            calculateTagInfo(estimatedPose, targets) { tagPose, estimatedTranslation ->
                val deltaX = tagPose.x - estimatedTranslation.x
                val deltaY = tagPose.y - estimatedTranslation.y
                val deltaZ = tagPose.z - estimatedTranslation.z
                sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)
            }

        currentStdDevs3d =
            calculateStdDevs(
                tagInfo.numTags,
                tagInfo.avgDistance,
                singleTargetVector,
                multiTargetVector,
            )
    }

    /**
     * Helper class to store tag information
     *
     * @property numTags The number of tags.
     * @property avgDistance The average distance of the tags.
     */
    private data class TagInfo(
        val numTags: Int,
        val avgDistance: Double,
    )

    /**
     * Calculate the number of visible tags and their average distance
     *
     * @param estimatedPose The estimated robot pose to use for updating the standard deviations.
     * @param targets The list of tracked targets to use for updating the standard deviations.
     * @param distanceCalculator The function to calculate the distance between the tag and the estimated translation.
     * @return TagInfo The number of tags and their average distance.
     */
    private fun calculateTagInfo(
        estimatedPose: Optional<EstimatedRobotPose>,
        targets: List<PhotonTrackedTarget>,
        distanceCalculator: (
            tagPose: Pose3d,
            estimatedTranslation: Pose3d,
        ) -> Double,
    ): TagInfo {
        var numTags = 0
        var totalDistance = 0.0

        for (target in targets) {
            val tagPoseOptional = poseEstimator.fieldTags.getTagPose(target.fiducialId)
            if (!tagPoseOptional.isPresent) continue

            numTags++
            val tagPose = tagPoseOptional.get()
            val estimatedTranslation = estimatedPose.get().estimatedPose
            totalDistance += distanceCalculator(tagPose, estimatedTranslation)
        }

        val avgDistance = if (numTags > 0) totalDistance / numTags else 0.0
        return TagInfo(numTags, avgDistance)
    }

    /**
     * Calculate standard deviations based on tag information
     *
     * @param numTags The number of tags.
     * @param avgDistance The average distance of the tags.
     * @param singleTargetVector The standard deviations for a single target.
     * @param multiTargetVector The standard deviations for multiple targets.
     * @return T The calculated standard deviations.
     */
    private fun <T : Matrix<*, N1>> calculateStdDevs(
        numTags: Int,
        avgDistance: Double,
        singleTargetVector: T,
        multiTargetVector: T,
    ): T {
        if (numTags == 0) {
            return singleTargetVector
        }

        val stdDevs = if (numTags > 1) multiTargetVector else singleTargetVector

        @Suppress("UNCHECKED_CAST")
        return (
            if (numTags == 1 && avgDistance > DISTANCE_THRESHOLD) {
                when (stdDevs.numRows) {
                    3 -> VecBuilder.fill(MAX_VALUE, MAX_VALUE, MAX_VALUE)
                    4 -> VecBuilder.fill(MAX_VALUE, MAX_VALUE, MAX_VALUE, MAX_VALUE)
                    else -> stdDevs
                }
            } else {
                stdDevs.times(1 + (avgDistance * avgDistance / DISTANCE_SCALE_FACTOR))
            }
        ) as T
    }
}
