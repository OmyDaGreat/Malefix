package xyz.malefic.frc.vision

import edu.wpi.first.math.VecBuilder
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Pose3d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Rotation3d
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.math.geometry.Translation3d
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.units.Units
import edu.wpi.first.units.measure.Angle

/**
 * The [LimeModule] class represents a Limelight camera with NetworkTables integration.
 *
 * Provides access to Limelight data including target detection, pose estimation,
 * and camera configuration via NetworkTables. Implements the [Camera] interface
 * for unified vision system integration.
 *
 * ## Usage Example:
 * ```kotlin
 * val limelight = LimeModule("limelight")
 *
 * limelight.configure {
 *     ledMode = LimeModule.LEDMode.OFF
 *     camMode = LimeModule.CamMode.VISION
 *     pipeline = 0
 * }
 *
 * // Get latest measurement
 * val measurement = limelight.getLatestMeasurement()
 * measurement?.let { swerveDrive.addVisionMeasurement(it.pose, it.timestampSeconds, it.stdDevs) }
 * ```
 *
 * @property name The name of the Limelight in NetworkTables (e.g., "limelight").
 */
class LimeModule(
    override val name: String,
) : Camera {
    private val table: NetworkTable = NetworkTableInstance.getDefault().getTable(name)

    /**
     * Configuration for the Limelight.
     *
     * @property ledMode LED operating mode.
     * @property camMode Camera operating mode.
     * @property pipeline Current vision pipeline (0-9).
     * @property stream Sets the streaming mode for multiple cameras.
     */
    data class Config(
        var ledMode: LEDMode = LEDMode.PIPELINE,
        var camMode: CamMode = CamMode.VISION,
        var pipeline: Int = 0,
        var stream: StreamMode = StreamMode.STANDARD,
    )

    /**
     * LED operating modes for Limelight.
     */
    enum class LEDMode(
        val value: Int,
    ) {
        /** Use LED mode specified in pipeline. */
        PIPELINE(0),

        /** Force LEDs off. */
        OFF(1),

        /** Force LEDs to blink. */
        BLINK(2),

        /** Force LEDs on. */
        ON(3),
    }

    /**
     * Camera operating modes for Limelight.
     */
    enum class CamMode(
        val value: Int,
    ) {
        /** Vision processing mode. */
        VISION(0),

        /** Driver camera mode (no processing). */
        DRIVER(1),
    }

    /**
     * Streaming modes for multiple Limelights.
     */
    enum class StreamMode(
        val value: Int,
    ) {
        /** Standard: Side-by-side streams if another camera is available. */
        STANDARD(0),

        /** PiP Main: Secondary camera in lower-right corner. */
        PIP_MAIN(1),

        /** PiP Secondary: Secondary camera takes up full screen. */
        PIP_SECONDARY(2),
    }

    /**
     * Configures the Limelight using a DSL-style configuration block.
     *
     * ```kotlin
     * limelight.configure {
     *     ledMode = LimeModule.LEDMode.OFF
     *     camMode = LimeModule.CamMode.VISION
     *     pipeline = 0
     * }
     * ```
     *
     * @param block Configuration block to apply settings.
     */
    fun configure(block: Config.() -> Unit) {
        val config = Config().apply(block)
        setLEDMode(config.ledMode)
        setCamMode(config.camMode)
        setPipeline(config.pipeline)
        setStreamMode(config.stream)
    }

    // Camera interface implementation
    override fun hasTarget(): Boolean = table.getEntry("tv").getDouble(0.0) == 1.0

    override fun getTargetHorizontalOffset(): Angle? = if (hasTarget()) Units.Degrees.of(getTx()) else null

    override fun getTargetVerticalOffset(): Angle? = if (hasTarget()) Units.Degrees.of(getTy()) else null

    override fun getLatestMeasurement(referencePose: Pose2d?): VisionMeasurement? {
        if (!hasTarget()) return null

        val botPose = getBotPose() ?: return null
        val latency = getLatency()
        val timestamp = (System.currentTimeMillis() / 1000.0) - latency

        // Limelight doesn't provide detailed target info, so use defaults
        // Could be improved with json parsing for detailed target data
        val stdDevs = VecBuilder.fill(0.7, 0.7, 0.5)

        return VisionMeasurement(
            pose = botPose,
            timestamp = Units.Seconds.of(timestamp),
            targetsUsed = if (hasTarget()) 1 else 0,
            stdDevs = stdDevs,
            averageTagDistance = Units.Meters.of(0.0), // Not directly available from Limelight
            ambiguity = 0.0, // Not directly available from basic NetworkTables
        )
    }

    override fun getAllUnreadMeasurements(): List<VisionMeasurement> {
        // Limelight doesn't buffer results like PhotonVision
        // Return single measurement if available
        val measurement = getLatestMeasurement()
        return if (measurement != null) listOf(measurement) else emptyList()
    }

    /**
     * Sets the LED operating mode.
     *
     * @param mode The desired LED mode.
     */
    fun setLEDMode(mode: LEDMode) {
        table.getEntry("ledMode").setNumber(mode.value)
    }

    /**
     * Sets the camera operating mode.
     *
     * @param mode The desired camera mode.
     */
    fun setCamMode(mode: CamMode) {
        table.getEntry("camMode").setNumber(mode.value)
    }

    /**
     * Sets the active vision pipeline.
     *
     * @param pipeline Pipeline index (0-9).
     */
    fun setPipeline(pipeline: Int) {
        table.getEntry("pipeline").setNumber(pipeline)
    }

    /**
     * Sets the streaming mode.
     *
     * @param mode The desired stream mode.
     */
    fun setStreamMode(mode: StreamMode) {
        table.getEntry("stream").setNumber(mode.value)
    }

    /**
     * Gets the horizontal offset from crosshair to target in degrees.
     *
     * @return Horizontal offset in degrees (-29.8 to 29.8).
     */
    fun getTx(): Double = table.getEntry("tx").getDouble(0.0)

    /**
     * Gets the vertical offset from crosshair to target in degrees.
     *
     * @return Vertical offset in degrees (-24.85 to 24.85).
     */
    fun getTy(): Double = table.getEntry("ty").getDouble(0.0)

    /**
     * Gets the target area as a percentage of the image.
     *
     * @return Target area (0% to 100% of image).
     */
    fun getTa(): Double = table.getEntry("ta").getDouble(0.0)

    /**
     * Gets the target skew or rotation in degrees.
     *
     * @return Skew in degrees (-90 to 0).
     */
    fun getTs(): Double = table.getEntry("ts").getDouble(0.0)

    /**
     * Gets the pipeline's latency contribution in milliseconds.
     *
     * @return Latency in milliseconds.
     */
    fun getLatencyPipeline(): Double = table.getEntry("tl").getDouble(0.0)

    /**
     * Gets the total latency (pipeline + capture) in milliseconds.
     *
     * @return Total latency in milliseconds.
     */
    fun getLatencyCapture(): Double = table.getEntry("cl").getDouble(0.0)

    /**
     * Gets the total latency in seconds (for use with pose estimators).
     *
     * @return Total latency in seconds.
     */
    fun getLatency(): Double = (getLatencyPipeline() + getLatencyCapture()) / 1000.0

    /**
     * Gets the robot's 2D pose in field space using MegaTag (botpose).
     *
     * @return Robot pose in field coordinates, or null if unavailable.
     */
    fun getBotPose(): Pose2d? {
        val poseArray = table.getEntry("botpose").getDoubleArray(DoubleArray(0))
        if (poseArray.size < 6) return null
        return Pose2d(
            Translation2d(poseArray[0], poseArray[1]),
            Rotation2d.fromDegrees(poseArray[5]),
        )
    }

    /**
     * Gets the robot's 3D pose in field space using MegaTag (botpose).
     *
     * @return Robot pose in field coordinates, or null if unavailable.
     */
    fun getBotPose3d(): Pose3d? {
        val poseArray = table.getEntry("botpose").getDoubleArray(DoubleArray(0))
        if (poseArray.size < 6) return null
        return Pose3d(
            Translation3d(poseArray[0], poseArray[1], poseArray[2]),
            Rotation3d(
                Math.toRadians(poseArray[3]),
                Math.toRadians(poseArray[4]),
                Math.toRadians(poseArray[5]),
            ),
        )
    }

    /**
     * Gets the robot's 2D pose in field space using MegaTag with blue alliance origin.
     *
     * @return Robot pose in blue alliance coordinates, or null if unavailable.
     */
    fun getBotPoseBlue(): Pose2d? {
        val poseArray = table.getEntry("botpose_wpiblue").getDoubleArray(DoubleArray(0))
        if (poseArray.size < 6) return null
        return Pose2d(
            Translation2d(poseArray[0], poseArray[1]),
            Rotation2d.fromDegrees(poseArray[5]),
        )
    }

    /**
     * Gets the robot's 2D pose in field space using MegaTag with red alliance origin.
     *
     * @return Robot pose in red alliance coordinates, or null if unavailable.
     */
    fun getBotPoseRed(): Pose2d? {
        val poseArray = table.getEntry("botpose_wpired").getDoubleArray(DoubleArray(0))
        if (poseArray.size < 6) return null
        return Pose2d(
            Translation2d(poseArray[0], poseArray[1]),
            Rotation2d.fromDegrees(poseArray[5]),
        )
    }

    /**
     * Gets the camera's 3D pose in robot space.
     *
     * @return Camera pose relative to robot, or null if unavailable.
     */
    fun getCameraPose(): Transform3d? {
        val poseArray = table.getEntry("camerapose_robotspace").getDoubleArray(DoubleArray(0))
        if (poseArray.size < 6) return null
        return Transform3d(
            Translation3d(poseArray[0], poseArray[1], poseArray[2]),
            Rotation3d(
                Math.toRadians(poseArray[3]),
                Math.toRadians(poseArray[4]),
                Math.toRadians(poseArray[5]),
            ),
        )
    }

    /**
     * Gets the target's 3D pose in camera space.
     *
     * @return Target pose relative to camera, or null if unavailable.
     */
    fun getTargetPoseCameraSpace(): Transform3d? {
        val poseArray = table.getEntry("targetpose_cameraspace").getDoubleArray(DoubleArray(0))
        if (poseArray.size < 6) return null
        return Transform3d(
            Translation3d(poseArray[0], poseArray[1], poseArray[2]),
            Rotation3d(
                Math.toRadians(poseArray[3]),
                Math.toRadians(poseArray[4]),
                Math.toRadians(poseArray[5]),
            ),
        )
    }

    /**
     * Gets the target's 3D pose in robot space.
     *
     * @return Target pose relative to robot, or null if unavailable.
     */
    fun getTargetPoseRobotSpace(): Transform3d? {
        val poseArray = table.getEntry("targetpose_robotspace").getDoubleArray(DoubleArray(0))
        if (poseArray.size < 6) return null
        return Transform3d(
            Translation3d(poseArray[0], poseArray[1], poseArray[2]),
            Rotation3d(
                Math.toRadians(poseArray[3]),
                Math.toRadians(poseArray[4]),
                Math.toRadians(poseArray[5]),
            ),
        )
    }

    /**
     * Gets the ID of the primary AprilTag in view.
     *
     * @return AprilTag ID, or null if no AprilTag detected.
     */
    fun getAprilTagId(): Int? {
        val id = table.getEntry("tid").getDouble(-1.0).toInt()
        return if (id >= 0) id else null
    }

    /**
     * Takes a snapshot with the current settings.
     */
    fun takeSnapshot() {
        table.getEntry("snapshot").setNumber(1)
    }
}
