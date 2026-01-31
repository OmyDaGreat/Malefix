package xyz.malefic.frc.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.Matrix
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.math.numbers.N1
import edu.wpi.first.math.numbers.N3
import xyz.malefic.frc.swerve.SwerveDrive
import xyz.malefic.frc.vision.command.AlignToTarget
import xyz.malefic.frc.vision.command.ResetPoseFromVision
import xyz.malefic.frc.vision.command.TrackTarget

/**
 * DSL for creating a [VisionSystem] with multiple cameras.
 *
 * ## Usage Example:
 * ```kotlin
 * val vision = visionSystem {
 *     config {
 *         enablePoseUpdates = true
 *         maxPoseAmbiguity = 0.2
 *     }
 *
 *     photon("frontCamera") {
 *         position = Transform3d(Translation3d(0.3, 0.0, 0.2), Rotation3d())
 *         fieldLayout = AprilTagFields.k2024Crescendo.loadAprilTagLayoutField()
 *     }
 *
 *     limelight("limelight") {
 *         ledMode = LimeModule.LEDMode.OFF
 *         pipeline = 0
 *     }
 *
 *     poseEstimator { pose, timestamp, stdDevs ->
 *         swerveDrive.addVisionMeasurement(pose, timestamp, stdDevs)
 *     }
 *
 *     currentPoseSupplier { swerveDrive.getPose() }
 * }
 * ```
 *
 * @param block Configuration block for the vision system.
 * @return Configured [VisionSystem].
 */
fun visionSystem(block: VisionSystemBuilder.() -> Unit): VisionSystem = VisionSystemBuilder().apply(block).build()

/**
 * Builder for creating a [VisionSystem] with DSL syntax.
 */
class VisionSystemBuilder {
    private val system = VisionSystem()
    private val photonBuilders = mutableListOf<PhotonModuleBuilder>()
    private val limeBuilders = mutableListOf<LimeModuleBuilder>()

    /**
     * Configures the vision system.
     *
     * @param block Configuration block.
     */
    fun config(block: VisionSystem.Config.() -> Unit) {
        system.configure(block)
    }

    /**
     * Adds a PhotonVision camera.
     *
     * @param name Name of the camera.
     * @param block Configuration block for the camera.
     */
    fun photon(
        name: String,
        block: PhotonModuleBuilder.() -> Unit,
    ) {
        photonBuilders.add(PhotonModuleBuilder(name).apply(block))
    }

    /**
     * Adds a Limelight camera.
     *
     * @param name Name of the Limelight.
     * @param block Configuration block for the Limelight.
     */
    fun limelight(
        name: String,
        block: LimeModuleBuilder.() -> Unit,
    ) {
        limeBuilders.add(LimeModuleBuilder(name).apply(block))
    }

    /**
     * Sets the pose estimator callback.
     *
     * @param callback Function to update pose with vision measurements.
     */
    fun poseEstimator(callback: (Pose2d, Double, Matrix<N3, N1>) -> Unit) {
        system.setPoseEstimator(callback)
    }

    /**
     * Sets the current pose supplier.
     *
     * @param supplier Function to get the current robot pose.
     */
    fun currentPoseSupplier(supplier: () -> Pose2d) {
        system.setCurrentPoseSupplier(supplier)
    }

    /**
     * Builds the configured vision system.
     *
     * @return Configured [VisionSystem].
     */
    fun build(): VisionSystem {
        photonBuilders.forEach { system.addPhotonModule(it.build()) }
        limeBuilders.forEach { system.addLimeModule(it.build()) }
        return system
    }
}

/**
 * Builder for creating a [PhotonModule].
 */
class PhotonModuleBuilder(
    private val name: String,
) {
    var position: Transform3d = Transform3d()
    var fieldLayout: AprilTagFieldLayout? = null

    /**
     * Builds the PhotonModule.
     *
     * @return Configured [PhotonModule].
     */
    fun build(): PhotonModule {
        val layout = fieldLayout
        require(layout != null) { "fieldLayout must be specified for PhotonModule" }
        return PhotonModule(name, position, layout)
    }
}

/**
 * Builder for creating a [LimeModule].
 */
class LimeModuleBuilder(
    private val name: String,
) {
    private val config = LimeModule.Config()

    var ledMode: LimeModule.LEDMode
        get() = config.ledMode
        set(value) {
            config.ledMode = value
        }

    var camMode: LimeModule.CamMode
        get() = config.camMode
        set(value) {
            config.camMode = value
        }

    var pipeline: Int
        get() = config.pipeline
        set(value) {
            config.pipeline = value
        }

    var stream: LimeModule.StreamMode
        get() = config.stream
        set(value) {
            config.stream = value
        }

    /**
     * Builds the LimeModule.
     *
     * @return Configured [LimeModule].
     */
    fun build(): LimeModule =
        LimeModule(name).apply {
            configure {
                ledMode = this@LimeModuleBuilder.ledMode
                camMode = this@LimeModuleBuilder.camMode
                pipeline = this@LimeModuleBuilder.pipeline
                stream = this@LimeModuleBuilder.stream
            }
        }
}

/**
 * Creates an [AlignToTarget] command for this swerve drive.
 *
 * @param camera The camera to use for alignment.
 * @param translationSpeed Supplier for forward/backward speed.
 * @param strafeSpeed Supplier for left/right speed.
 * @return [AlignToTarget] command.
 */
fun SwerveDrive.alignToTarget(
    camera: Camera,
    translationSpeed: () -> Double = { 0.0 },
    strafeSpeed: () -> Double = { 0.0 },
) = AlignToTarget(this, camera, translationSpeed, strafeSpeed)

/**
 * Creates a [TrackTarget] command for this swerve drive.
 *
 * @param camera The camera to use for tracking.
 * @param translationSpeed Supplier for forward/backward speed.
 * @param strafeSpeed Supplier for left/right speed.
 * @return [TrackTarget] command.
 */
fun SwerveDrive.trackTarget(
    camera: Camera,
    translationSpeed: () -> Double,
    strafeSpeed: () -> Double,
) = TrackTarget(this, camera, translationSpeed, strafeSpeed)

/**
 * Creates a [ResetPoseFromVision] command for this swerve drive.
 *
 * @param visionSystem The vision system to use.
 * @return [ResetPoseFromVision] command.
 */
fun SwerveDrive.resetPoseFromVision(visionSystem: VisionSystem) = ResetPoseFromVision(this, visionSystem)

/**
 * Extension to easily integrate a vision system with a swerve drive.
 *
 * ## Usage Example:
 * ```kotlin
 * val vision = visionSystem { ... }
 * swerveDrive.integrateVision(vision)
 * ```
 *
 * @param visionSystem The vision system to integrate.
 */
fun SwerveDrive.integrateVision(visionSystem: VisionSystem) {
    visionSystem.setPoseEstimator { pose, timestamp, stdDevs ->
        this.addVisionMeasurement(pose, timestamp)
    }
    visionSystem.setCurrentPoseSupplier { this.getPose() }
}
