package xyz.malefic.frc.vision

import edu.wpi.first.math.Matrix
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.numbers.N1
import edu.wpi.first.math.numbers.N3

/**
 * Represents a vision measurement from a camera.
 *
 * Contains the estimated robot pose, timestamp, number of targets used,
 * and standard deviations for pose estimation accuracy.
 *
 * @property pose The estimated robot pose on the field.
 * @property timestampSeconds The timestamp of the measurement in seconds.
 * @property targetsUsed The number of AprilTags or targets used for this estimate.
 * @property stdDevs The standard deviations for x, y, and theta (rotation).
 * @property averageTagDistance The average distance to all visible tags in meters.
 * @property ambiguity The pose ambiguity (0.0 = perfect, higher = more ambiguous).
 */
data class VisionMeasurement(
    val pose: Pose2d,
    val timestampSeconds: Double,
    val targetsUsed: Int,
    val stdDevs: Matrix<N3, N1>,
    val averageTagDistance: Double = 0.0,
    val ambiguity: Double = 0.0,
)
