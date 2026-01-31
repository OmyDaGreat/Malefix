package xyz.malefic.frc.vision

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.units.measure.Angle

/**
 * Common interface for all vision cameras (PhotonVision, Limelight, etc.).
 *
 * Provides a unified API for accessing vision data regardless of the underlying
 * camera implementation.
 *
 * ## Implementations:
 * - [PhotonModule] - PhotonVision camera
 * - [LimeModule] - Limelight camera
 */
interface Camera {
    /**
     * The name of this camera.
     */
    val name: String

    /**
     * Checks if the camera currently sees any valid targets.
     *
     * @return True if at least one target is visible, false otherwise.
     */
    fun hasTarget(): Boolean

    /**
     * Gets the latest vision measurement from the camera.
     *
     * @param referencePose Optional reference pose to improve pose estimation accuracy.
     * @return [VisionMeasurement] if available, null otherwise.
     */
    fun getLatestMeasurement(referencePose: Pose2d? = null): VisionMeasurement?

    /**
     * Gets the horizontal offset to the currently tracked target.
     *
     * This is used by alignment and tracking commands.
     *
     * @return Horizontal offset (positive = target is to the right), null if no target.
     */
    fun getTargetHorizontalOffset(): Angle?

    /**
     * Gets the vertical offset to the currently tracked target.
     *
     * @return Vertical offset (positive = target is above crosshair), null if no target.
     */
    fun getTargetVerticalOffset(): Angle?

    /**
     * Gets all unprocessed vision measurements since the last call.
     *
     * Some cameras (like PhotonVision) buffer multiple results. This method
     * returns all unread measurements.
     *
     * @return List of [VisionMeasurement]s, may be empty.
     */
    fun getAllUnreadMeasurements(): List<VisionMeasurement>
}
