package xyz.malefic.frc.vision

import edu.wpi.first.math.Matrix
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.numbers.N1
import edu.wpi.first.math.numbers.N3
import edu.wpi.first.units.Units
import edu.wpi.first.units.measure.Distance
import edu.wpi.first.units.measure.Time

/**
 * Represents a vision measurement from a camera.
 *
 * Contains the estimated robot pose, timestamp, number of targets used,
 * and standard deviations for pose estimation accuracy.
 *
 * @property pose The estimated robot pose on the field.
 * @property timestamp The timestamp of the measurement.
 * @property targetsUsed The number of AprilTags or targets used for this estimate.
 * @property stdDevs The standard deviations for x, y, and theta (rotation).
 * @property averageTagDistance The average distance to all visible tags.
 * @property ambiguity The pose ambiguity (0.0 = perfect, higher = more ambiguous, unitless).
 */
data class VisionMeasurement(
    val pose: Pose2d,
    val timestamp: Time,
    val targetsUsed: Int,
    val stdDevs: Matrix<N3, N1>,
    val averageTagDistance: Distance = Units.Meters.of(0.0),
    val ambiguity: Double = 0.0,
) {
    /**
     * Gets the timestamp in seconds for backward compatibility.
     */
    val timestampSeconds: Double
        get() = timestamp.`in`(Units.Seconds)
}
