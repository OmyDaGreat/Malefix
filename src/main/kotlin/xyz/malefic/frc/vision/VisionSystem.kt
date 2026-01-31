package xyz.malefic.frc.vision

import edu.wpi.first.math.Matrix
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.numbers.N1
import edu.wpi.first.math.numbers.N3
import edu.wpi.first.wpilibj2.command.SubsystemBase

/**
 * The [VisionSystem] subsystem manages multiple vision cameras for robot pose estimation
 * and target tracking.
 *
 * Supports multi-camera fusion, automatic pose updates, and provides methods for accessing
 * vision data from all connected cameras. Works with any camera implementing the [Camera] interface.
 *
 * ## Usage Example:
 * ```kotlin
 * val visionSystem = VisionSystem()
 *
 * visionSystem.configure {
 *     enablePoseUpdates = true
 *     maxPoseAmbiguity = 0.2
 * }
 *
 * // Add cameras
 * val frontCamera = PhotonModule(...)
 * val limelight = LimeModule("limelight")
 * visionSystem.addCamera(frontCamera)
 * visionSystem.addCamera(limelight)
 *
 * // Set the pose estimator to update (typically your SwerveDrive)
 * visionSystem.setPoseEstimator { pose, timestamp, stdDevs ->
 *     swerveDrive.addVisionMeasurement(pose, timestamp, stdDevs)
 * }
 * ```
 */
class VisionSystem : SubsystemBase() {
    /**
     * Configuration for the vision system.
     *
     * @property enablePoseUpdates Whether to automatically update pose estimates.
     * @property enableLogging Whether to log vision data to NetworkTables.
     * @property maxPoseAmbiguity Maximum allowed pose ambiguity for single-tag measurements.
     * @property minTargetsForPose Minimum number of targets required for pose estimation.
     * @property rejectOutlierPoses Whether to reject poses that are far from the current estimate.
     * @property maxPoseDistanceMeters Maximum allowed distance between vision pose and current pose.
     */
    data class Config(
        var enablePoseUpdates: Boolean = true,
        var enableLogging: Boolean = true,
        var maxPoseAmbiguity: Double = 0.2,
        var minTargetsForPose: Int = 1,
        var rejectOutlierPoses: Boolean = true,
        var maxPoseDistanceMeters: Double = 2.0,
    )

    private var config = Config()
    private val cameras = mutableListOf<Camera>()
    private var poseEstimatorCallback: ((Pose2d, Double, Matrix<N3, N1>) -> Unit)? = null
    private var currentPoseSupplier: (() -> Pose2d)? = null

    /**
     * Configures the vision system using a DSL-style configuration block.
     *
     * ```kotlin
     * visionSystem.configure {
     *     enablePoseUpdates = true
     *     maxPoseAmbiguity = 0.2
     * }
     * ```
     *
     * @param block Configuration block to apply settings.
     */
    fun configure(block: Config.() -> Unit) {
        config.apply(block)
    }

    /**
     * Adds a camera to the vision system.
     *
     * @param camera The camera to add (PhotonModule, LimeModule, or any Camera implementation).
     */
    fun addCamera(camera: Camera) {
        cameras.add(camera)
    }

    /**
     * Adds a PhotonVision camera module to the vision system.
     *
     * @param module The PhotonModule to add.
     */
    fun addPhotonModule(module: PhotonModule) {
        addCamera(module)
    }

    /**
     * Adds a Limelight camera module to the vision system.
     *
     * @param module The LimeModule to add.
     */
    fun addLimeModule(module: LimeModule) {
        addCamera(module)
    }

    /**
     * Sets the callback function for updating the robot's pose estimator.
     *
     * This function will be called with vision measurements from all cameras.
     *
     * @param callback Function that accepts (pose, timestamp, stdDevs).
     */
    fun setPoseEstimator(callback: (Pose2d, Double, Matrix<N3, N1>) -> Unit) {
        poseEstimatorCallback = callback
    }

    /**
     * Sets a supplier for getting the current robot pose.
     *
     * Used for rejecting outlier measurements and updating PhotonVision pose estimators.
     *
     * @param supplier Function that returns the current robot pose.
     */
    fun setCurrentPoseSupplier(supplier: () -> Pose2d) {
        currentPoseSupplier = supplier
    }

    /**
     * Gets all cameras.
     *
     * @return List of all cameras.
     */
    fun getCameras(): List<Camera> = cameras

    /**
     * Gets all PhotonVision camera modules.
     *
     * @return List of PhotonModules.
     */
    fun getPhotonModules(): List<PhotonModule> = cameras.filterIsInstance<PhotonModule>()

    /**
     * Gets all Limelight camera modules.
     *
     * @return List of LimeModules.
     */
    fun getLimeModules(): List<LimeModule> = cameras.filterIsInstance<LimeModule>()

    /**
     * Gets a camera by name.
     *
     * @param name The name of the camera.
     * @return The camera, or null if not found.
     */
    fun getCamera(name: String): Camera? = cameras.find { it.name == name }

    /**
     * Gets a PhotonVision camera module by name.
     *
     * @param name The name of the camera.
     * @return The PhotonModule, or null if not found.
     */
    fun getPhotonModule(name: String): PhotonModule? = getPhotonModules().find { it.name == name }

    /**
     * Gets a Limelight camera module by name.
     *
     * @param name The name of the Limelight.
     * @return The LimeModule, or null if not found.
     */
    fun getLimeModule(name: String): LimeModule? = getLimeModules().find { it.name == name }

    /**
     * Gets the best available vision measurement from all cameras.
     *
     * Prioritizes measurements with more targets and lower ambiguity.
     *
     * @return The best VisionMeasurement, or null if no valid measurement is available.
     */
    fun getBestVisionMeasurement(): VisionMeasurement? {
        val currentPose = currentPoseSupplier?.invoke()

        // Collect all measurements
        val measurements =
            cameras.mapNotNull { camera ->
                camera.getLatestMeasurement(currentPose)
            }

        // Return the best measurement (prefer more targets, then lower ambiguity)
        return measurements
            .filter { it.targetsUsed >= config.minTargetsForPose }
            .maxWithOrNull(
                compareBy<VisionMeasurement> { it.targetsUsed }
                    .thenBy { -(it.ambiguity) },
            )
    }

    /**
     * Checks if any camera currently sees a target.
     *
     * @return True if at least one camera sees a target.
     */
    fun hasTarget(): Boolean = cameras.any { it.hasTarget() }

    /**
     * Gets the number of visible targets across all cameras.
     *
     * @return Total number of targets.
     */
    fun getVisibleTargetCount(): Int = cameras.count { it.hasTarget() }

    override fun periodic() {
        if (!config.enablePoseUpdates || poseEstimatorCallback == null) return

        val currentPose = currentPoseSupplier?.invoke()

        // Process all cameras
        for (camera in cameras) {
            val measurements = camera.getAllUnreadMeasurements()

            for (measurement in measurements) {
                // Reject measurements with too few targets
                if (measurement.targetsUsed < config.minTargetsForPose) {
                    continue
                }

                // Reject outliers if enabled
                if (config.rejectOutlierPoses && currentPose != null) {
                    val distance = measurement.pose.translation.getDistance(currentPose.translation)
                    if (distance > config.maxPoseDistanceMeters) {
                        continue
                    }
                }

                // Add measurement to pose estimator
                poseEstimatorCallback?.invoke(
                    measurement.pose,
                    measurement.timestampSeconds,
                    measurement.stdDevs,
                )
            }
        }
    }

    /**
     * Enables or disables automatic pose updates.
     *
     * @param enabled Whether to enable pose updates.
     */
    fun setEnablePoseUpdates(enabled: Boolean) {
        config.enablePoseUpdates = enabled
    }

    /**
     * Sets all Limelight LEDs to a specific mode.
     *
     * @param mode The LED mode to set.
     */
    fun setAllLimelightLEDs(mode: LimeModule.LEDMode) {
        getLimeModules().forEach { it.setLEDMode(mode) }
    }

    /**
     * Sets all Limelight pipelines.
     *
     * @param pipeline The pipeline index to set (0-9).
     */
    fun setAllLimelightPipelines(pipeline: Int) {
        getLimeModules().forEach { it.setPipeline(pipeline) }
    }
}
